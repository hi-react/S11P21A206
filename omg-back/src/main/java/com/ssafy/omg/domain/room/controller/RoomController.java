package com.ssafy.omg.domain.room.controller;

import com.ssafy.omg.config.baseresponse.BaseException;
import com.ssafy.omg.config.baseresponse.BaseResponse;
import com.ssafy.omg.domain.room.dto.CommonRoomRequest;
import com.ssafy.omg.domain.room.dto.CommonRoomResponse;
import com.ssafy.omg.domain.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.reactor.ReactorProperties;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ReactorProperties reactorProperties;

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
//        userNickname = "test1";
        String roomId = roomService.createRoom(userNickname);
        log.info("Room created: {}, User: {}", roomId, userNickname);
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
        log.info("User {} entered room {}", request.getSender(), request.getRoomId());
        return new BaseResponse<>(response);
    }

    /**
     * @param request
     * @return response
     * @throws BaseException
     */
    @PostMapping("/leave")
    public BaseResponse<CommonRoomResponse> leaveRoom(@RequestBody CommonRoomRequest request) throws BaseException {
        CommonRoomResponse response = roomService.leaveRoom(request);
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
     * @param request
     * @return response
     * @throws BaseException
     */
    @PostMapping("/start")
    public BaseResponse<Object> clickStartButton(@RequestBody CommonRoomRequest request) throws BaseException {
        CommonRoomResponse response = roomService.clickStartButton(request);
        messagingTemplate.convertAndSend("/sub/game/" + request.getRoomId(),
                new CommonRoomResponse(request.getRoomId(), "GAME_START", null, response.getRoomInfo()));
        return new BaseResponse<>(response);
    }
}