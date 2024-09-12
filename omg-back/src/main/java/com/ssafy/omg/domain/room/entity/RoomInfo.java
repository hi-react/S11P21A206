package com.ssafy.omg.domain.room.entity;

import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomInfo implements Serializable {
    private String roomId;
    private String hostNickname;
    private Map<String, Boolean> inRoomPlayers;

    public RoomInfo(String roomId, String hostNickname) {
        this.roomId = roomId;
        this.hostNickname = hostNickname;
        this.inRoomPlayers = new HashMap<>();
        this.inRoomPlayers.put(hostNickname, false);
    }

    public void addPlayer(String nickname) {
        this.inRoomPlayers.put(nickname, false);
    }

    public void removePlayer(String nickname) {
        this.inRoomPlayers.remove(nickname);
    }

    public void setPlayerRendered(String nickname, boolean isRendered) {
        this.inRoomPlayers.put(nickname, isRendered);
    }

    public boolean isAllRendered() {
        return !this.inRoomPlayers.containsValue(false);
    }

    public int getPlayerCount() {
        return this.inRoomPlayers.size();
    }
}
