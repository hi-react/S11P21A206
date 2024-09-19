package com.ssafy.omg.domain.game.controller;

import com.ssafy.omg.config.baseresponse.BaseException;
import com.ssafy.omg.domain.game.entity.Game;
import com.ssafy.omg.domain.game.service.GameServiceImpl;
import com.ssafy.omg.domain.room.dto.CommonRoomRequest;
import com.ssafy.omg.domain.room.dto.CommonRoomResponse;
import com.ssafy.omg.domain.room.entity.Room;
import com.ssafy.omg.domain.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {
	private final GameServiceImpl gameService;
	private final RoomService roomService;
	private final SimpMessageSendingOperations messagingTemplate;

	@MessageMapping("/init")
	public void initializeGame(@Payload CommonRoomRequest request) throws BaseException {
		String roomId = request.getRoomId();
		Room roomInfo = roomService.getRoom(roomId);
		List<String> players = roomInfo.getInRoomPlayers()
				.stream()
				.map(player -> player.getNickname())
				.collect(Collectors.toList());

		Game game = new Game();

		messagingTemplate.convertAndSend("/pub/game/" + roomId,
				new CommonRoomResponse(roomId, request.getSender(), "GAME_INITIALIZED", game, null));
	}
}
