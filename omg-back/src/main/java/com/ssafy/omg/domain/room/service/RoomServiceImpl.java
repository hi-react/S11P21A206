package com.ssafy.omg.domain.room.service;

import com.ssafy.omg.config.baseresponse.BaseException;
import com.ssafy.omg.domain.general.entity.GeneralInfo;
import com.ssafy.omg.domain.room.dto.CommonRoomRequest;
import com.ssafy.omg.domain.room.dto.CommonRoomResponse;
import com.ssafy.omg.domain.room.entity.InRoomPlayer;
import com.ssafy.omg.domain.room.entity.RoomInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import static com.ssafy.omg.config.baseresponse.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RedisTemplate<String, GeneralInfo> redisTemplate;
    // Redis에서 대기방 식별을 위한 접두사 ROOM_PREFIX 설정
    private static final String ROOM_PREFIX = "room";
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ROOMID_LENGTH = 10;
    private static final int MAX_PLAYERS = 4;

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

            RoomInfo roomInfo = new RoomInfo(roomId, userNickname);
            System.out.println("RoomInfo : " + roomInfo);

            GeneralInfo generalInfo = GeneralInfo.builder()
                    .roomId(roomId)
                    .message("CREATE_ROOM_SUCCESS")
                    .roomInfo(roomInfo)
                    .build();

            // Redis에 대기방 정보 저장
            redisTemplate.opsForValue().set(roomKey, generalInfo, 1, TimeUnit.HOURS);

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
        validateRequest(roomId, sender);

        // 대기방 존재하지 않을 경우 예외처리
        GeneralInfo generalInfo = redisTemplate.opsForValue().get(roomKey);
        if (generalInfo == null || generalInfo.getRoomInfo() == null) {
            throw new BaseException(ROOM_NOT_FOUND);
        }

        // 대기방 인원이 4명이면 꽉 찼으므로 예외처리
        RoomInfo roomInfo = generalInfo.getRoomInfo();
        if (isRoomFull(roomInfo)) {
            throw new BaseException(ROOM_FULLED_ERROR);
        }

        boolean isNewPlayer = !isPlayerInRoom(roomInfo, sender);
        if (isNewPlayer) {
            addPlayer(roomInfo, sender);
            generalInfo.setRoomInfo(roomInfo);
            generalInfo.setMessage("ENTER_SUCCESS");
            redisTemplate.opsForValue().set(roomKey, generalInfo, 1, TimeUnit.HOURS);
        } else {
            throw new BaseException(ALREADY_ENTERED_ERROR);
        }

        return new CommonRoomResponse(roomId, "ENTER_SUCCESS", null, roomInfo);
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
        validateRequest(roomId, sender);

        GeneralInfo generalInfo = redisTemplate.opsForValue().get(roomKey);
        if (generalInfo == null || generalInfo.getRoomInfo() == null) {
            throw new BaseException(ROOM_NOT_FOUND);
        }

        RoomInfo roomInfo = generalInfo.getRoomInfo();
        // 대기방 존재하지 않을 경우 예외처리
        if (roomInfo == null) {
            throw new BaseException(ROOM_NOT_FOUND);
        }

        // 대기방에 존재하지 않는 사람일 경우 예외처리
        if (!isPlayerInRoom(roomInfo, sender)) {
            throw new BaseException(USER_NOT_IN_ROOM);
        }

        removePlayer(roomInfo, sender);

        if (getPlayerCount(roomInfo) == 0) {
            redisTemplate.delete(roomKey);
            return new CommonRoomResponse(roomId, "ROOM_DELETED", null, null);
        } else if (sender.equals(roomInfo.getHostNickname())) {
            String newHost = roomInfo.getInRoomPlayers().get(0).getNickname();
            roomInfo.setHostNickname(newHost);
        }

        generalInfo.setRoomInfo(roomInfo);
        generalInfo.setMessage("LEAVE_ROOM");
        redisTemplate.opsForValue().set(roomKey, generalInfo, 1, TimeUnit.HOURS);
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
        GeneralInfo generalInfo = redisTemplate.opsForValue().get(roomKey);

        if (generalInfo == null || generalInfo.getRoomInfo() == null) {
            throw new BaseException(ROOM_NOT_FOUND);
        }

        RoomInfo roomInfo = generalInfo.getRoomInfo();
        boolean isHost = roomInfo.getHostNickname().equals(sender);
        return isHost && getPlayerCount(roomInfo) == MAX_PLAYERS;
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
        RoomInfo roomInfo = getRoomInfo(roomId);
        return new CommonRoomResponse(roomId, "START_BUTTON_CLICKED", null, roomInfo);
    }

    /**
     * 사용자 렌더 완료
     *
     * @param request
     * @return
     * @throws BaseException
     */
    @Override
    public CommonRoomResponse handleRenderedComplete(CommonRoomRequest request) throws BaseException {
        String roomKey = ROOM_PREFIX + request.getRoomId();
        String roomId = request.getRoomId();
        String sender = request.getSender();
        GeneralInfo generalInfo = redisTemplate.opsForValue().get(roomKey);

        if (generalInfo == null || generalInfo.getRoomInfo() == null) {
            throw new BaseException(ROOM_NOT_FOUND);
        }

        //Todo 유저별 렌더링 유무 변수를 일정 시간마다 체크해 true일 시 렌더링카운트 증가
        RoomInfo roomInfo = generalInfo.getRoomInfo();
        setPlayerRendered(roomInfo, sender, true);
        generalInfo.setRoomInfo(roomInfo);
        generalInfo.setMessage("RENDERED_COMPLETE");
        redisTemplate.opsForValue().set(roomKey, generalInfo, 1, TimeUnit.HOURS);

//        GameInfo gameInfo = gameService.getGameInfo(roomId);
//        if (gameInfo == null) {
//            throw new BaseException(GAME_NOT_FOUND);
//        }

        return new CommonRoomResponse(roomId, "RENDERED_COMPLETE", null, roomInfo);
    }

    /**
     * 모든 사용자 렌더 완료 여부
     *
     * @param request
     * @return
     * @throws BaseException
     */
    @Override
    public CommonRoomResponse checkAllRenderedCompleted(CommonRoomRequest request) throws BaseException {
        String roomKey = ROOM_PREFIX + request.getRoomId();
        String roodId = request.getRoomId();
        GeneralInfo generalInfo = redisTemplate.opsForValue().get(roomKey);

        if (generalInfo == null) {
            throw new BaseException(ROOM_NOT_FOUND);
        }

        RoomInfo roomInfo = generalInfo.getRoomInfo();
        if (isAllRendered(roomInfo)) {
            generalInfo.setMessage("RENDER_COMPLETE_ACCEPTED");
            redisTemplate.opsForValue().set(roomKey, generalInfo, 1, TimeUnit.HOURS);
            return new CommonRoomResponse(roodId, "RENDER_COMPLETE_ACCEPTED", null, roomInfo);
        } else {
            throw new BaseException(RENDER_NOT_COMPLETED);
        }
    }

    /**
     * 방 정보 반환
     *
     * @param roomId
     * @return
     * @throws BaseException
     */
    @Override
    public RoomInfo getRoomInfo(String roomId) throws BaseException {
        String roomKey = ROOM_PREFIX + roomId;
        GeneralInfo generalInfo = redisTemplate.opsForValue().get(roomKey);
        if (generalInfo == null || generalInfo.getRoomInfo() == null) {
            throw new BaseException(ROOM_NOT_FOUND);
        }
        RoomInfo roomInfo = generalInfo.getRoomInfo();
        return roomInfo;
    }

    /**
     * 방 정보 업데이트
     *
     * @param room
     * @throws BaseException
     */
    @Override
    public void updateRoom(RoomInfo room) throws BaseException {
        String roomKey = ROOM_PREFIX + room.getRoomId();
        GeneralInfo generalInfo = redisTemplate.opsForValue().get(roomKey);
        generalInfo.setRoomInfo(room);
        redisTemplate.opsForValue().set(roomKey, generalInfo, 1, TimeUnit.HOURS);
    }

    /**
     * 방이 가득 찼는지 확인
     *
     * @param roomInfo 확인할 방 정보
     * @return 방이 가득 찼으면 true, 그렇지 않으면 false
     */
    private boolean isRoomFull(RoomInfo roomInfo) {
        return getPlayerCount(roomInfo) >= MAX_PLAYERS;
    }

    /**
     * 특정플레이어 방에 있는지 확인
     *
     * @param roomInfo 확인할 방 정보
     * @param nickname 확인할 플레이어의 닉네임
     * @return 플레이어가 방에 있으면 true, 없으면 false
     */
    private boolean isPlayerInRoom(RoomInfo roomInfo, String nickname) {
        return roomInfo.getInRoomPlayers().stream()
                .anyMatch(player -> player.getNickname().equals(nickname));
    }

    /**
     * 플레이어 추가
     *
     * @param roomInfo 플레이어를 추가할 방 정보
     * @param nickname 추가할 플레이어의 닉네임
     */
    private void addPlayer(RoomInfo roomInfo, String nickname) {
        roomInfo.getInRoomPlayers().add(new InRoomPlayer(nickname, false));
    }

    /**
     * 특정 플레이어를 제거
     *
     * @param roomInfo 플레이어를 제거할 방 정보
     * @param nickname 제거할 플레이어의 닉네임
     */
    private void removePlayer(RoomInfo roomInfo, String nickname) {
        roomInfo.getInRoomPlayers().removeIf(player -> player.getNickname().equals(nickname));
    }

    /**
     * 방 내부 현재 플레이어수 반환
     *
     * @param roomInfo 플레이어 수를 확인할 방 정보
     * @return 방의 현재 플레이어 수
     */
    private int getPlayerCount(RoomInfo roomInfo) {
        return roomInfo.getInRoomPlayers().size();
    }

    /**
     * 모든 플레이어 렌더링여부 확인
     *
     * @param roomInfo 확인할 방 정보
     * @return 모든 플레이어가 렌더링을 완료했으면 true, 그렇지 않으면 false
     */
    private boolean isAllRendered(RoomInfo roomInfo) {
        return roomInfo.getInRoomPlayers().stream().allMatch(InRoomPlayer::isRendered);
    }

    /**
     * 특정 플레이어 렌더링 상태를 설정
     *
     * @param roomInfo   플레이어가 있는 방 정보
     * @param nickname   렌더링 상태를 설정할 플레이어의 닉네임
     * @param isRendered 설정할 렌더링 상태
     */
    private void setPlayerRendered(RoomInfo roomInfo, String nickname, boolean isRendered) {
        roomInfo.getInRoomPlayers().stream()
                .filter(player -> player.getNickname().equals(nickname))
                .findFirst()
                .ifPresent(player -> player.setRendered(isRendered));
    }

    /**
     * 요청의 입력유효성 검사
     *
     * @param sender 요청을 보낸 사용자의 닉네임
     * @throws BaseException roomId나 sender가 null이거나 비어있을 경우 발생
     */
    private void validateRequest(String roomId, String sender) throws BaseException {
        if (roomId == null || roomId.isEmpty() || sender == null || sender.isEmpty()) {
            throw new BaseException(REQUEST_ERROR);
        }
    }

    /**
     * Redis에서 GeneralInfo를 가져오기
     *
     * @param roomKey Redis에서 사용하는 방의 키
     * @return 방의 GeneralInfo
     * @throws BaseException 방을 찾을 수 없을 경우 발생
     */
    private GeneralInfo getGeneralInfo(String roomKey) throws BaseException {
        GeneralInfo generalInfo = redisTemplate.opsForValue().get(roomKey);
        if (generalInfo == null || generalInfo.getRoomInfo() == null) {
            throw new BaseException(ROOM_NOT_FOUND);
        }
        return generalInfo;
    }

}