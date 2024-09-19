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


	// 대출

	/**
	 * 대출
	 * 0. 거래소에서 사채업자 클릭해 대출 선택
	 * 1. (preLoan) 대출 가능한 금액과 횟수 표시
	 * 2. (preLoan) 대출 여부를 체크해서 대출 가능 여부 판단
	 * 3. (takeLoan) 가능 대출 범위 내에서 원하는 금액 대출
	 * 4. (takeLoan) 대출금을 자산에 반영
	 * 5. 대출 종료
	 * <p>
	 * 대출횟수는 1회
	 * <p>
	 * 가능 대출 범위 :
	 * 주가수준 0~2일때 : 50~100
	 * 주가수준 3~5일때 : 150~300
	 * 주가수준 6~9일때 : 500~1000
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

		player.setHasLoan(1);
		player.setLoan(amount);
		player.setInterestRate(arena.getGame().getInterestRate());
		player.setCash(player.getCash() + amount);

		savePlayer(roomKey, arena, player);
	}

	// 상환


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
	 * @param arena
	 * @param sender
	 * @return
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
	 * player의 행위로 인해 변경된 값을 redis에 적용
	 *
	 * @param arena
	 * @param currPlayer
	 * @return
	 */
	private void savePlayer(String roomKey, Arena arena, Player currPlayer) {
		arena.getGame().getPlayers().replaceAll(player ->
				player.getNickname().equals(currPlayer.getNickname()) ? currPlayer : player
		);

		redisTemplate.opsForValue().set(roomKey, arena);
	}
}
