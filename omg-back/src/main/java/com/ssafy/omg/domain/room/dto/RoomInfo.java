package com.ssafy.omg.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomInfo {
    private String gameId;
    private HostInfo host;
    private List<String> inRoomPlayers;
    private int isRendered;
}
