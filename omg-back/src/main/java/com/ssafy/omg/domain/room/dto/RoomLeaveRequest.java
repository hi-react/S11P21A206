package com.ssafy.omg.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomLeaveRequest {
	private String gameId;
	private String sender;
	private String message;
}
