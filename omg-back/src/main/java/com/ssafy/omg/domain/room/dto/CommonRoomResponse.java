package com.ssafy.omg.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommonRoomResponse {
    private String roomId;
    private String message;
    private Object game;
    private RoomInfo room;
}
