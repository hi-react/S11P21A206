package com.ssafy.omg.domain.room.dto;

import com.ssafy.omg.domain.game.dto.GameInfo;
import com.ssafy.omg.domain.game.entity.Game;
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
    private GameInfo game;
    private RoomInfo roomInfo;
}
