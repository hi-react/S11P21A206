package com.ssafy.omg.domain.room.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomInfo {
	private String gameId;
	private HostInfo host;
	private List<String> inRoomPlayers;
	private int isRendered;
}
