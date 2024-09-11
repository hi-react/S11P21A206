package com.ssafy.omg.domain.game.controller;

import com.ssafy.omg.config.baseresponse.BaseException;
import com.ssafy.omg.domain.game.dto.GameInfo;
import com.ssafy.omg.domain.game.dto.CommonUserActionRequest;
import com.ssafy.omg.domain.game.service.GameService;
import com.ssafy.omg.domain.room.dto.CommonRoomRequest;
import com.ssafy.omg.domain.room.dto.CommonRoomResponse;
import com.ssafy.omg.domain.room.dto.RoomInfo;
import com.ssafy.omg.domain.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final RoomService roomService;
    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/init")
    public void initializeGame(@Payload CommonRoomRequest request) throws BaseException {
        String roomId = request.getRoomId();
        RoomInfo roomInfo = roomService.getRoomInfo(roomId);
        List<String> players = roomInfo.getInRoomPlayers();

        GameInfo gameInfo = gameService.initializeGame(roomId, players);

        messagingTemplate.convertAndSend("/topic/game/" + roomId,
                new CommonRoomResponse(roomId, "GAME_INITIALIZED", gameInfo, null));
    }

	// TODO
	// 선택행동: 대출, 상환
	// 필수행동: 주식매수(+0주), 주식매도(+0주), 금괴매입
	// 행동 전 체크: 상환, 주식매수, 금괴매입

	/**
	 * 대출
	 *
	 * @param request
	 * @throws BaseException
	 */
	@MessageMapping("/game.loan")
	public void takeLoan(@Payload CommonUserActionRequest request) throws BaseException {
		gameService.takeLoan(request);
	}
}
