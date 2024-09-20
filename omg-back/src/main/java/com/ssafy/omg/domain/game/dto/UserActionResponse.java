package com.ssafy.omg.domain.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserActionResponse {
	private String gameId;
	private String message;
	private Action action;
	private GameSimpleInfo game;
	private String reason;
}
