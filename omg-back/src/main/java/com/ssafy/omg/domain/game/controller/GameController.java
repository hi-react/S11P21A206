package com.ssafy.omg.domain.game.controller;

import com.ssafy.omg.config.baseresponse.BaseException;
import com.ssafy.omg.domain.game.dto.CommonUserActionRequest;
import com.ssafy.omg.domain.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GameController {

	private final GameService gameService;

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
