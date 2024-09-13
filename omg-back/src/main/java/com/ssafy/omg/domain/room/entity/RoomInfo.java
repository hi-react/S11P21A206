package com.ssafy.omg.domain.room.entity;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomInfo implements Serializable {
    private String roomId;
    private String hostNickname;
    private List<Map<String, Boolean>> inRoomPlayers;

    public RoomInfo(String roomId, String hostNickname) {
        this.roomId = roomId;
        this.hostNickname = hostNickname;
        this.inRoomPlayers = new ArrayList<>();
        addPlayer(hostNickname);
    }

    public void addPlayer(String nickname) {
        Map<String, Boolean> player = new HashMap<>();
        player.put(nickname, false);
        this.inRoomPlayers.add(player);
    }

    public void removePlayer(String nickname) {
        this.inRoomPlayers.removeIf(player -> player.containsKey(nickname));
    }

    public void setPlayerRendered(String nickname, boolean isRendered) {
        for (Map<String, Boolean> player : inRoomPlayers) {
            if (player.containsKey(nickname)) {
                player.put(nickname, isRendered);
            }
        }
    }

    public boolean isAllRendered() {
        for (Map<String, Boolean> player : inRoomPlayers) {
            if (player.containsValue(false)) {
                return false;
            }
        }
        return true;
    }

    public int getPlayerCount() {
        return this.inRoomPlayers.size();
    }
}
