package com.ssafy.omg.domain.general.entity;

import com.ssafy.omg.domain.game.entity.Game;
import com.ssafy.omg.domain.room.entity.RoomInfo;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralInfo implements Serializable {
    private String roomId;
    private String message;
    private Game game;
    private RoomInfo roomInfo;
}
