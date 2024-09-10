package com.ssafy.omg.domain.game.entity;

public class Game {
    // private String gameId; // 게임 세션 아이디
    // private String message; // 게임 통신 규약
    // private HashMap<Integer, Player> players; // 역할 : player 객체 정보 (순서, 닉네임, 금, 현금, 주식별 개수, 대출, )
    // private int turn; // 현재 턴
    // private int round; // 현재 라운드
    // private GameStatus gameStatus; // 현재 게임 진행 상태 (시작 전, 진행 중, 게임 종료)
    // public enum GameMode {
    // 	ONE_VS_ONE(2), ONE_VS_THREE(4);
    // }
    //
    // public enum GameStatus {
    // 	BEFORE_START, IN_GAME, GAME_FINISHED
    // }

    /**
     * 게임 정보
     * 		- 금리, 경제카드, 주가수준, 금괴가격, 주머니, 금괴 매입개수별 추가,
     * 		- 주식 시장(종류별 개수, 주가), [매수트랙 매도트랙 금괴트랙], 라운드 수, 라운드 별 행위 순서,
     * 		- 한 라운드 시간, 주가 변동 여부 체크, 주가수준 계산(2차원배열?)
     * 플레이어 정보
     * 		- 대출 유무, 대출금,이자율, 현금, 금괴개수, 보유 주식, 플레이어 좌표(x, y, z),
     * 		- 플레이어 행위(무슨 행동을 하는지), 플레이어 행위 종료 상태, 플레이어 접속 상태, 한 행위 시간
     * 행위
     *
     */
}
