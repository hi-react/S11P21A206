package com.ssafy.omg.domain.room.service;

import com.ssafy.omg.config.baseresponse.BaseException;
import com.ssafy.omg.domain.game.service.GameService;
import com.ssafy.omg.domain.room.dto.CommonRoomRequest;
import com.ssafy.omg.domain.room.dto.CommonRoomResponse;
import com.ssafy.omg.domain.room.dto.HostInfo;
import com.ssafy.omg.domain.room.dto.RoomInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.ssafy.omg.config.baseresponse.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RedisTemplate<String, RoomInfo> redisTemplate;
    // Redis에서 대기방 식별을 위한 접두사 ROOM_PREFIX 설정
    private static final String ROOM_PREFIX = "room";
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ROOMID_LENGTH = 10;
    private final GameService gameService;

    /**
     * 고유한 대기방 ID 생성
     *
     * @return roomId 고유한 대기방 ID
     */
    @Override
    public String createRoomId() throws BaseException {
        StringBuilder stringBuilder = new StringBuilder(ROOMID_LENGTH);
        SecureRandom random = new SecureRandom();

        while (stringBuilder.length() < ROOMID_LENGTH) {
            int randomIndex = random.nextInt(ALPHA_NUMERIC_STRING.length());
            stringBuilder.append(ALPHA_NUMERIC_STRING.charAt(randomIndex));
        }

        String roomId = stringBuilder.toString();

        // 대기방 Id 중복검사
        if (Boolean.TRUE.equals(redisTemplate.hasKey(ROOM_PREFIX + roomId))) {
            return createRoomId();
        }

        return roomId;
    }

    /**
     * 대기 방 생성
     *
     * @param userNickname
     * @return
     */
    @Override
    public String createRoom(String userNickname) throws BaseException {
        try {
            String roomId = createRoomId();
            String roomKey = ROOM_PREFIX + roomId;
            System.out.println("방 아이디 : " + roomId);

            RoomInfo roomInfo = new RoomInfo(roomId, new HostInfo(userNickname), new ArrayList<>(), 0);
            roomInfo.getInRoomPlayers().add(userNickname);
            System.out.println("RoomInfo : " + roomInfo);

            // Redis에 대기방 정보 저장
            redisTemplate.opsForValue().set(roomKey, roomInfo, 1, TimeUnit.HOURS);

            return roomId;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(ROOM_CREATION_ERROR);
        }
    }

    /**
     * 대기 방 입장
     * 방이 빈 경우 roomInfo 생성
     * roomInfo에 접속자 nickname 없는 경우 추가
     *
     * @param request
     * @return CommonRoomResponse
     */
    @Override
    public CommonRoomResponse enterRoom(CommonRoomRequest request) throws BaseException {
        String roomKey = ROOM_PREFIX + request.getRoomId();
        String roomId = request.getRoomId();
        String sender = request.getSender();
        String message = request.getMessage();

        // 입력값 오류
        if (roomId == null || roomId.isEmpty()) {
            throw new BaseException(REQUEST_ERROR);
        }
        if (sender == null || sender.isEmpty()) {
            throw new BaseException(REQUEST_ERROR);
        }

        // 대기방 존재하지 않을 경우 예외처리
        RoomInfo roomInfo = redisTemplate.opsForValue().get(roomKey);
        if (roomInfo == null) {
            throw new BaseException(ROOM_NOT_FOUND);
        }

        // 대기방 인원이 4명이면 꽉 찼으므로 예외처리
        if (roomInfo.getInRoomPlayers().size() >= 4) {
            throw new BaseException(ROOM_FULLED_ERROR);
        }

        boolean isNewPlayer = !roomInfo.getInRoomPlayers().contains(sender);
        if (isNewPlayer) {
            roomInfo.getInRoomPlayers().add(sender);
            redisTemplate.opsForValue().set(roomKey, roomInfo, 1, TimeUnit.HOURS);
        } else {
            throw new BaseException(ALREADY_ENTERED_ERROR);
        }

        return new CommonRoomResponse(roomId, "ENTER_SUCSESS", null, roomInfo);

    }

    /**
     * 대기 방 나가기
     * 방장이 방을 나가면 그 다음 inRoomPlayer중 처음인 사람을 newHost로 설정
     *
     * @param request
     * @return
     */
    @Override
    public CommonRoomResponse leaveRoom(CommonRoomRequest request) throws BaseException {
        String roomKey = ROOM_PREFIX + request.getRoomId();
        String roomId = request.getRoomId();
        String sender = request.getSender();

        // 입력값 오류
        if (roomId == null || roomId.isEmpty()) {
            throw new BaseException(REQUEST_ERROR);
        }
        if (sender == null || sender.isEmpty()) {
            throw new BaseException(REQUEST_ERROR);
        }

        RoomInfo roomInfo = redisTemplate.opsForValue().get(roomKey);

        // 대기방 존재하지 않을 경우 예외처리
        if (roomInfo == null) {
            throw new BaseException(ROOM_NOT_FOUND);
        }

        // 대기방에 존재하지 않는 사람일 경우 예외처리
        if (!roomInfo.getInRoomPlayers().contains(sender)) {
            throw new BaseException(USER_NOT_IN_ROOM);
        }

        roomInfo.getInRoomPlayers().remove(sender);

        if (roomInfo.getInRoomPlayers().isEmpty()) {
            redisTemplate.delete(roomKey);
            return new CommonRoomResponse(roomId, "ROOM_DELETED", null, null);
        } else if (sender.equals(roomInfo.getHost().getNickname())) {
            String newHost = roomInfo.getInRoomPlayers().get(0);
            roomInfo.setHost(new HostInfo(newHost));
        }
        redisTemplate.opsForValue().set(roomKey, roomInfo, 1, TimeUnit.HOURS);
        return new CommonRoomResponse(roomId, "LEAVE_ROOM", null, roomInfo);
    }

    /**
     * 시작 버튼 활성화 여부
     * 방장이면서 4명이 꽉 찼을 때 game start 버튼 활성화
     *
     * @param request
     * @return
     */
    @Override
    public boolean isStartButtonActive(CommonRoomRequest request) throws BaseException {
        String roomKey = ROOM_PREFIX + request.getRoomId();
        String sender = request.getSender();
        RoomInfo roomInfo = redisTemplate.opsForValue().get(roomKey);

        if (roomInfo == null) {
            throw new BaseException(ROOM_NOT_FOUND);
        }

        boolean isHost = roomInfo.getHost().getNickname().equals(sender);
        return isHost && roomInfo.getInRoomPlayers().size() == 4;
    }

    /**
     * 시작 버튼 클릭
     *
     * @param request
     * @return
     */
    @Override
    public CommonRoomResponse clickStartButton(CommonRoomRequest request) throws BaseException {
        String roomId = request.getRoomId();
        if (!isStartButtonActive(request)) {
            throw new BaseException(INSUFFICIENT_PLAYER_ERROR);
        }
        return new CommonRoomResponse(roomId, "START_BUTTON_CLICKED", null, null);
    }

}