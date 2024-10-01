package com.ssafy.omg.domain.game.service;

import com.ssafy.omg.config.baseresponse.BaseException;
import com.ssafy.omg.domain.arena.entity.Arena;
import com.ssafy.omg.domain.game.GameRepository;
import com.ssafy.omg.domain.game.dto.GameStatusDto;
import com.ssafy.omg.domain.game.dto.PlayerResponse;
import com.ssafy.omg.domain.game.dto.PlayerStateDto;
import com.ssafy.omg.domain.game.entity.Game;
import com.ssafy.omg.domain.game.entity.GameStatus;
import com.ssafy.omg.domain.player.entity.Player;
import com.ssafy.omg.domain.socket.dto.StompPayload;
import com.ssafy.omg.domain.socket.dto.StompResponsePayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ssafy.omg.config.baseresponse.BaseResponseStatus.ARENA_NOT_FOUND;
import static com.ssafy.omg.config.baseresponse.BaseResponseStatus.GAME_NOT_FOUND;
import static com.ssafy.omg.config.baseresponse.BaseResponseStatus.PLAYER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Getter
public class GameBroadcastService {

    private final Map<String, ScheduledExecutorService> schedulerMap = new ConcurrentHashMap<>();
    private final SimpMessageSendingOperations messagingTemplate;
    private final GameRepository gameRepository;


    public void startBroadcast(String roomId) {
        log.info("Starting broadcast for roomId: {}", roomId);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                broadcastGameState(roomId);
            } catch (BaseException e) {
                log.error("방 {} 브로드캐스트 실패", roomId, e);
                stopBroadcast(roomId);
            }
         }, 0, 16, TimeUnit.MILLISECONDS);
//        }, 0, 20000, TimeUnit.MILLISECONDS);
        schedulerMap.put(roomId, scheduler);
    }

    public void stopBroadcast(String roomId) {
        log.info("Stopping broadcast for roomId: {}", roomId);
        ScheduledExecutorService scheduler = schedulerMap.get(roomId);
        if(scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            schedulerMap.remove(roomId);
        }
    }

    private void broadcastGameState(String roomId) throws BaseException {
        Arena arena = gameRepository.findArenaByRoomId(roomId)
                .orElseThrow(() -> new BaseException(ARENA_NOT_FOUND));

        Game game = arena.getGame();
        if (game == null) {
            log.warn("No found for roomId: {}", roomId);
            throw new BaseException(GAME_NOT_FOUND);
        }
        // TODO 필요에 따라 데이터 수정
        List<PlayerStateDto> playerStateDtos = game.getPlayers().stream()
                .map(p -> new PlayerStateDto(p.getNickname(), p.getPosition(), p.getDirection(), p.sendActionToggle()))
                .collect(Collectors.toList());

        StompResponsePayload<List<PlayerStateDto>> payload = new StompResponsePayload<>("PLAYER_STATE", playerStateDtos);

        log.debug("send payload roomId = {}", roomId);
        messagingTemplate.convertAndSend("/sub/" + roomId + "/game", payload);
    }
}
