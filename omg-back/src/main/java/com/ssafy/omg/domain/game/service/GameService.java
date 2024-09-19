package com.ssafy.omg.domain.game.service;

import com.ssafy.omg.config.baseresponse.BaseException;

public interface GameService {
	int preLoan(String roomId, String sender) throws BaseException;

	void takeLoan(String roomId, String sender, int amount) throws BaseException;
}
