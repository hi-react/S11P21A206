package com.ssafy.omg.domain.game.service;

import com.ssafy.omg.config.baseresponse.BaseException;
import com.ssafy.omg.domain.arena.entity.Arena;
import com.ssafy.omg.domain.game.controller.GameController;
import com.ssafy.omg.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.ssafy.omg.config.baseresponse.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final RedisTemplate<String, Arena> redisTemplate;
    // Redis에서 대기방 식별을 위한 접두사 ROOM_PREFIX 설정
    private static final String ROOM_PREFIX = "room";
    // 대출 가능 범위
    private final int[][] LOAN_RANGE = new int[][]{{50, 100}, {150, 300}, {500, 1000}};
    private final GameController gameController;

    // 초기화
//	public GameInfo initializeGame(String gameId, List<String> players) {
//		GameInfo gameInfo = new GameInfo();
//
//		gameInfo.setGameId(gameId);
//		gameInfo.setCurrentPosition(new int[]{0, 0, 0, 0});
//		gameInfo.setTurn(1);
//		gameInfo.setRound(1);
//		gameInfo.setGameStatus("BEFORE_GAME_PLAY");
//		gameInfo.setStartTime(java.time.LocalDateTime.now().toString());
//
//		Map<String, PlayerInfo> playerInfoMap = new HashMap<>();
//		for (int i = 0; i < players.size(); i++) {
//			PlayerInfo playerInfo = new PlayerInfo();
//			playerInfo.setNickname(players.get(i));
//			playerInfo.setGold(0);
//			playerInfo.setCash(200);
//			playerInfo.setToken(new int[]{0, 1, 2, 1, 1});
//			playerInfoMap.put(String.valueOf(i), playerInfo);
//		}
//		gameInfo.setPlayers(playerInfoMap);
//
//		redisTemplate.opsForValue().set("game:" + gameId, gameInfo);
//
//		return gameInfo;
//	}

    // TODO
    // preLoan을 api 따로 빼면 수정
    // 플레이어 행위 상태(state)는 언제 어디서 업데이트 해줘야 할지


    // 대출

    /**
     * [preLoan] 대출 가능 여부 판단 후, 대출 금액 범위 리턴
     *
     * @param
     * @throws BaseException
     */
    public int preLoan(String roomId, String sender) throws BaseException {
        String roomKey = ROOM_PREFIX + roomId;

        // 입력값 오류
        validateRequest(roomId, sender);

        Arena arena = getArena(roomKey);
        Player player = getPlayer(arena, sender);

        // 이미 대출을 받은 적 있으면 0 리턴
        if (player.getHasLoan() == 1) {
            throw new BaseException(LOAN_ALREADY_TAKEN);
        }

        // 대출 가능 금액 계산
        int stockPriceLevel = arena.getGame().getStockPriceLevel();
        if (stockPriceLevel < 0 || stockPriceLevel > 9) {
            throw new BaseException(INVALID_STOCK_LEVEL);
        } else if (stockPriceLevel <= 2) {
            return 0;
        } else if (stockPriceLevel <= 5) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * [takeLoan] 요청금액 대출 후, 플레이어 자산 업데이트
     *
     * @param roomId
     * @param sender
     * @param amount
     * @throws BaseException
     */
    public void takeLoan(String roomId, String sender, int amount) throws BaseException {
        String roomKey = ROOM_PREFIX + roomId;

        validateRequest(roomId, sender);
        int range = preLoan(roomId, sender);

        // 요청 금액이 대출 한도를 이내인지 검사
        if (amount <= LOAN_RANGE[range][0] || LOAN_RANGE[range][1] <= amount) {
            throw new BaseException(AMOUNT_OUT_OF_RANGE);
        }

        // 대출금을 자산에 반영
        Arena arena = getArena(roomKey);
        Player player = getPlayer(arena, sender);
        int interest = amount * (arena.getGame().getInterestRate() / 100);
        player = player.toBuilder()
                .hasLoan(1)
                .loan(amount)
                .interest(interest)
                .debt(amount + interest)
                .cash(player.getCash() + amount).build();

        savePlayer(roomKey, arena, player);
    }

    // 상환
    public void repayLoan(String roomId, String sender, int amount) throws BaseException {
        String roomKey = ROOM_PREFIX + roomId;

        validateRequest(roomId, sender);

        Arena arena = getArena(roomKey);
        Player player = getPlayer(arena, sender);

        // 상환 금액이 유효한 값인지 판단(음수, 갚아야 할 금액보다 큰 금액인 경우, 자신이 보유한 현금보다 큰 값인 경우)
        if (amount < 0 || player.getDebt() < amount || player.getCash() < amount) {
            throw new BaseException(INVALID_REPAY_AMOUNT);
        }

        // 상환 후 자산에 반영(갚아야 할 금액 차감, 현금 차감)
        player = player.toBuilder()
                .debt(player.getDebt() - amount)
                .cash(player.getCash() - amount)
                .build();

        savePlayer(roomKey, arena, player);
    }


    // 주식 매수


    // 주식 매도


    // 금괴 매입


    /**
     * 요청의 입력유효성 검사
     *
     * @param sender 요청을 보낸 사용자의 닉네임
     * @throws BaseException roomId나 sender가 null이거나 비어있을 경우 발생
     */
    private void validateRequest(String roomId, String sender) throws BaseException {
        if (roomId == null || roomId.isEmpty() || sender == null || sender.isEmpty()) {
            throw new BaseException(REQUEST_ERROR);
        }
    }

    /**
     * Redis에서 arena를 가져오기
     *
     * @param roomKey Redis에서 사용하는 방의 키
     * @return 방의 arena
     * @throws BaseException 방을 찾을 수 없을 경우 발생
     */
    private Arena getArena(String roomKey) throws BaseException {
        Arena arena = redisTemplate.opsForValue().get(roomKey);
        if (arena == null || arena.getGame() == null) {
            throw new BaseException(GAME_NOT_FOUND);
        }
        return arena;
    }


    /**
     * arena에서 sender에 해당하는 player 가져오기
     *
     * @param arena
     * @param sender
     * @return arena game의 players 중 sender에 해당하는 player
     * @throws BaseException
     */
    private Player getPlayer(Arena arena, String sender) throws BaseException {
        List<Player> players = arena.getGame().getPlayers();
        Optional<Player> currPlayer = players.stream()
                .filter(player -> player.getNickname().equals(sender))
                .findFirst();

        if (currPlayer.isEmpty()) {
            throw new BaseException(PLAYER_NOT_FOUND);
        }
        return currPlayer.get();
    }

    /**
     * player의 행위로 인해 변경된 값을 redis에 업데이트
     *
     * @param arena
     * @param currPlayer
     */
    private void savePlayer(String roomKey, Arena arena, Player currPlayer) {
        arena.getGame().getPlayers().replaceAll(player ->
                player.getNickname().equals(currPlayer.getNickname()) ? currPlayer : player
        );

        redisTemplate.opsForValue().set(roomKey, arena);
    }
}
