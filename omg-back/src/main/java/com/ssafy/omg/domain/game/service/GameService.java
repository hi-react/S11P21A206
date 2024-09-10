package com.ssafy.omg.domain.game.service;

import com.ssafy.omg.domain.game.dto.GameInfo;
import com.ssafy.omg.domain.game.dto.PlayerInfo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameService {
    public GameInfo initializeGame(String gameId, List<String> players) {
        GameInfo gameInfo = new GameInfo();
        gameInfo.setGameId(gameId);
        gameInfo.setCurrentPosition(new int[]{0, 0, 0, 0});
        gameInfo.setTurn(1);
        gameInfo.setRound(1);
        gameInfo.setGameStatus("BEFORE_START");
        gameInfo.setStartTime(java.time.LocalDateTime.now().toString());

        Map<String, PlayerInfo> playerInfoMap = new HashMap<>();
        for (int i = 0; i < players.size(); i++) {
            PlayerInfo playerInfo = new PlayerInfo();
            playerInfo.setNickname(players.get(i));
            playerInfo.setGold(0);
            playerInfo.setCash(200);
            playerInfo.setToken(new int[]{0, 1, 2, 1, 1});
            playerInfoMap.put(String.valueOf(i), playerInfo);
        }
        gameInfo.setPlayers(playerInfoMap);

        return gameInfo;
    }
}
