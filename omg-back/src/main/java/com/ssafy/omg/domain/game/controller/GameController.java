package com.ssafy.omg.domain.game.controller;

import com.ssafy.omg.config.baseresponse.BaseException;
import com.ssafy.omg.domain.game.dto.GameInfo;
import com.ssafy.omg.domain.game.service.GameService;
import com.ssafy.omg.domain.room.dto.CommonRoomRequest;
import com.ssafy.omg.domain.room.dto.CommonRoomResponse;
import com.ssafy.omg.domain.room.entity.RoomInfo;
import com.ssafy.omg.domain.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
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
}
