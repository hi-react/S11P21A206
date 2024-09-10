package com.ssafy.omg.domain.room.service;

import com.ssafy.omg.domain.game.GameService;
import com.ssafy.omg.domain.room.dto.CommonRoomRequest;
import com.ssafy.omg.domain.room.dto.CommonRoomResponse;
import com.ssafy.omg.domain.room.dto.HostInfo;
import com.ssafy.omg.domain.room.dto.RoomInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final Map<String, RoomInfo> rooms = new HashMap<>();
    private final GameService gameService;

    @Override
    public CommonRoomResponse enterRoom(CommonRoomRequest request) {
        String gameId = request.getGameId();
        String sender = request.getSender();

        RoomInfo roomInfo = rooms.get(gameId);
        if (roomInfo == null) {
            roomInfo = new RoomInfo(gameId, new HostInfo(sender), new ArrayList<>(), 0);
            rooms.put(gameId, roomInfo);
        }

        if (!roomInfo.getInRoomPlayers().contains(sender)) {
            roomInfo.getInRoomPlayers().add(sender);
        }

        CommonRoomResponse response = new CommonRoomResponse();

    }

    @Override
    public CommonRoomResponse leaveRoom(CommonRoomRequest request) {
        // Implementation for leaving a room
        return null;
    }

    @Override
    public CommonRoomResponse startButtonClicked(CommonRoomRequest request) {
        // Implementation for handling start button click
        return null;
    }

    @Override
    public CommonRoomResponse renderedComplete(CommonRoomRequest request) {
        // Implementation for handling render complete
        return null;
    }
}