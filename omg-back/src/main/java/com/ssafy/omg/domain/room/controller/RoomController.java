package com.ssafy.omg.domain.room.controller;

import com.ssafy.omg.config.baseresponse.BaseException;
import com.ssafy.omg.config.baseresponse.BaseResponse;
import com.ssafy.omg.domain.game.entity.Game;
import com.ssafy.omg.domain.general.entity.GeneralInfo;
import com.ssafy.omg.domain.room.dto.CommonRoomRequest;
import com.ssafy.omg.domain.room.dto.CommonRoomResponse;
import com.ssafy.omg.domain.room.entity.RoomInfo;
import com.ssafy.omg.domain.room.service.RoomService;
import com.ssafy.omg.domain.socket.dto.StompPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final RedisTemplate<String, GeneralInfo> redisTemplate;
    private static final String ROOM_PREFIX = "room";

    // TODO
    // User 정보 AccessToken에서 HttpServletRequest로부터 getAttribute해서 userNickname 가져올 것.
    // 임시로 닉네임 부여해서 테스트 진행

    /**
     * 대기방 생성
     *
     * @param userNickname
     * @return response
     * @throws BaseException
     */
    @PostMapping("/create")
    public BaseResponse<String> createRoom(@RequestParam String userNickname) throws BaseException {
        String roomId = roomService.createRoom(userNickname);
        log.info("방 생성: {}, 사용자: {}", roomId, userNickname);
        return new BaseResponse<>(roomId);
    }

    /**
     * 대기방 입장
     *
     * @param request
     * @return response
     * @throws BaseException
     */
    @PostMapping("/enter")
    public BaseResponse<CommonRoomResponse> enterRoom(@RequestBody CommonRoomRequest request) throws BaseException {
        CommonRoomResponse response = roomService.enterRoom(request);
        log.info("사용자 {} 방 입장 {}", request.getSender(), request.getRoomId());
        return new BaseResponse<>(response);
    }

    /**
     * 대기방 나가기
     *
     * @param request
     * @return response
     * @throws BaseException
     */
    @PostMapping("/leave")
    public BaseResponse<CommonRoomResponse> leaveRoom(@RequestBody CommonRoomRequest request) throws BaseException {
        CommonRoomResponse response = roomService.leaveRoom(request);
        log.info("사용자 {} 방 나가기 {}", request.getSender(), request.getRoomId());
        return new BaseResponse<>(response);
    }

    /**
     * 게임 시작 버튼 활성화
     *
     * @param request
     * @return response
     * @throws BaseException
     */
    @GetMapping("")
    public BaseResponse<Boolean> isStartButtonActive(@RequestBody CommonRoomRequest request) throws BaseException {
        boolean isButtonActive = roomService.isStartButtonActive(request);
        return new BaseResponse<>(isButtonActive);
    }

    /**
     * 게임 시작 버튼 클릭
     * 게임 초기화 진행
     * 로딩중 화면으로 넘어가면서 렌더링 체크함
     * 모든 사용자가 렌더링 완료시 게임 시작
     *
     * @param request
     * @return response
     * @throws BaseException
     */
    @PostMapping("/start")
    public BaseResponse<Object> clickStartButton(@RequestBody CommonRoomRequest request) throws BaseException {
        CommonRoomResponse response = roomService.clickStartButton(request);
        messagingTemplate.convertAndSend("/sub/game/" + request.getRoomId(),
                new CommonRoomResponse(request.getRoomId(), "GAME_START", null, response.getRoomInfo()));
        log.info("게임 시작 , 방 코드 {}", request.getRoomId());
        return new BaseResponse<>(response);
    }


    /**
     * ================================================== WEBSOCKET ==================================================
     * 게임 초기화 처리
     * 모든 플레이어의 렌더링 상태를 초기화하고 게임 시작 메시지를 전송
     *
     * @param roomId  게임 방 ID
     * @param payload 초기화 요청 정보
     * @throws BaseException 게임 초기화 중 오류 발생 시
     */
    @MessageMapping("/init/{roomId}")
    public void initializeGame(@DestinationVariable String roomId, @Payload StompPayload<Void> payload) throws BaseException {
        log.info("게임 초기화, 방 아이디 : {}", roomId);
        GeneralInfo generalInfo = redisTemplate.opsForValue().get(roomId);
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);

        //TODO
        // 게임 초기 세팅 진행
        // 아래 gameInfo:null에 게임 초기화한 데이터 넣어야함 바꾸기
        Game game = generalInfo.getGame();

        CommonRoomResponse response = new CommonRoomResponse(roomId, "GAME_INITIALIZED", null, roomInfo);
        messagingTemplate.convertAndSend("/sub/init/" + roomId, response);
    }

    /**
     * 플레이어의 렌더링 완료 처리
     * 개별 플레이어의 렌더링 상태를 업데이트하고 모든 플레이어에게 상태를 브로드캐스트
     *
     * @param roomId  게임 방 ID
     * @param payload 렌더링 완료 정보
     * @throws BaseException 렌더링 상태 업데이트 중 오류 발생 시
     */
    @MessageMapping("/init-rendered/{roomId}")
    public void handleRenderedComplete(@DestinationVariable String roomId, @Payload StompPayload<Void> payload) throws BaseException {
        log.info("사용자 {} 렌더링 완료, 방 코드 : {}", payload.getSender(), roomId);

        // 특정 사용자 렌더링여부 true로
        RoomInfo room = roomService.getRoomInfo(roomId);
        room.setPlayerRendered(payload.getSender(), true);
        roomService.updateRoom(room);

        StompPayload<RoomInfo> playerRenderedResponse = new StompPayload<>("PLAYER_RENDERED", roomId, payload.getSender(), room);
        messagingTemplate.convertAndSend("/sub/init/" + roomId + "/rendered", playerRenderedResponse);

        if (room.isAllRendered()) {
            StompPayload<RoomInfo> allRenderedResponse = new StompPayload<>("ALL_RENDERED", roomId, null, room);
            messagingTemplate.convertAndSend("/sub/init/" + roomId, allRenderedResponse);
        }
    }

    /**
     * 모든 플레이어의 렌더링 상태 확인
     * 현재 방의 모든 플레이어의 렌더링 상태를 확인하고 결과를 전송
     *
     * @param roomId  게임 방 ID
     * @param payload 렌더링 상태 확인 요청 정보
     * @throws BaseException 렌더링 상태 확인 중 오류 발생 시
     */
    @MessageMapping("/init-checkAllRendered/{roomId}/")
    public void checkAllRenderedCompleted(@DestinationVariable String roomId, @Payload StompPayload<Void> payload) throws BaseException {
        log.info("모든 사용자 렌더링 여부 체크, 방 코드 :  {}", roomId);

        RoomInfo room = roomService.getRoomInfo(roomId);
        boolean allRendered = room.isAllRendered();

        String status = allRendered ? "ALL_RENDERED" : "RENDERING_IN_PROGRESS";
        CommonRoomResponse response = new CommonRoomResponse(roomId, status, null, room);
        messagingTemplate.convertAndSend("/sub/init/" + roomId, response);
    }
}