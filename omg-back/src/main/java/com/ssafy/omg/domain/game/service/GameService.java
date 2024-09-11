package com.ssafy.omg.domain.game.service;

import com.ssafy.omg.domain.game.dto.CommonUserActionRequest;
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

    /**
     * 대출
     * 0. 거래소에서 사채업자 클릭해 대출 선택
     * 1. (preLoan) 대출 가능한 금액과 횟수 표시
     * 2. (preLoan) 대출 여부를 체크해서 대출 가능 여부 판단
     * 3. (takeLoan) 가능 대출 범위 내에서 원하는 금액 대출
     * 4. (takeLoan) 대출금을 자산에 반영
     * 5. 대출 종료
     *
     * 대출횟수는 1회
     *
     * 가능 대출 범위 :
     *      주가수준 0~2일때 : 50~100
     *      주가수준 3~5일때 : 150~300
     *      주가수준 6~9일때 : 500~1000
     *
     * @param request
     */
    public void takeLoan(CommonUserActionRequest request) {

    }


}
