package com.ssafy.omg.domain.room.entity;

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
public class Room implements Serializable {
    private String roomId;
    private String hostNickname;
    private List<String> inRoomPlayers;
    private int renderedCount;
}
