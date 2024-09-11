package com.ssafy.omg.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomInfo implements Serializable {
    private String roomId;
    private HostInfo host;
    private List<String> inRoomPlayers;
    private int isRendered;
}
