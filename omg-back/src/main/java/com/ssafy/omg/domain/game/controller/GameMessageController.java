package com.ssafy.omg.domain.game.controller;

import com.ssafy.omg.config.baseresponse.BaseException;
import com.ssafy.omg.domain.game.dto.GameEventDto;
import com.ssafy.omg.domain.game.entity.GameEvent;
import com.ssafy.omg.domain.game.service.GameService;
import com.ssafy.omg.domain.socket.dto.StompPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import static com.ssafy.omg.config.baseresponse.BaseResponseStatus.REQUEST_ERROR;

@Slf4j
@Controller
@EnableScheduling
@RequiredArgsConstructor
@CrossOrigin("*")
public class GameMessageController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final GameService gameService;

    @MessageMapping("/game-event")
    public void createGameEvent(@Payload StompPayload<String> gameEventPayload, StompHeaderAccessor accessor) throws BaseException {
        String roomId = gameEventPayload.getRoomId();
        String sessionId = accessor.getSessionId();
        String message = gameEventPayload.getData();

        StompPayload<GameEventDto> response = new StompPayload<>();

        if (roomId == null || message == null) {
            log.error("Invalid payload: roomId or message is null");
            throw new BaseException(REQUEST_ERROR);
        }

        switch (message) {
            case "ECONOMIC_EVENT_OCCURED":
//                try {
                GameEvent gameEvent = gameService.createGameEventandInterestChange(roomId);
                if (gameEvent == null) {
                    log.error("GameEvent is null for roomId: {}", roomId);
                    return;
                }

                GameEventDto responseDto = new GameEventDto(
                        "ECONOMIC_EVENT_APPLIED",
                        gameEvent.getTitle(),
                        gameEvent.getContent(),
                        gameEvent.getValue()
                );
                response = new StompPayload<>("ECONOMIC_EVENT_APPLIED", roomId, "GAME_MANAGER", responseDto);
//                } catch (BaseException e) {
//                    log.error("Error applying economic event: {}", e.getMessage());
//                    GameEventDto errorDto = new GameEventDto("ERROR", "Error", e.getMessage(), 0);
//                    response = new StompPayload<>("ERROR", roomId, "SYSTEM", errorDto);
//                }
                break;

            default:
                log.warn("{} 에 일치하는 메시지 타입이 없습니다.", message);
                return;
        }

        // /sub/{roomId}/game 구독하는 사용자에게 모두 전송
        messagingTemplate.convertAndSend("/sub/" + roomId + "/game", response);
    }
}
