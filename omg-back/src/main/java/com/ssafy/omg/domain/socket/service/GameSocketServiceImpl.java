package com.ssafy.omg.domain.socket.service;

import com.ssafy.omg.domain.room.dto.CommonRoomRequest;
import com.ssafy.omg.domain.room.dto.CommonRoomResponse;
import com.ssafy.omg.domain.room.entity.RoomInfo;
import com.ssafy.omg.domain.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameSocketServiceImpl {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    // 방 정보 수정
    public void modifyRoom(CommonRoomRequest request) {
        try {
            RoomInfo roomInfo = roomService.getRoomInfo(request.getRoomId());
            // 방 정보 수정 로직
            // ...

            CommonRoomResponse response = new CommonRoomResponse(request.getRoomId(), "ROOM_MODIFIED", null, roomInfo);
            messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), response);
        } catch (Exception e) {
            log.error("Error modifying room: ", e);
        }
    }

    public void gameInit(CommonRoomRequest request) {
        try {
            CommonRoomResponse response = roomService.clickStartButton(request);
            messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), response);
        } catch (Exception e) {
            log.error("Error initializing game: ", e);
        }
    }

    public void hideStart(String roomId) {
        try {
            RoomInfo roomInfo = roomService.getRoomInfo(roomId);
            // 숨기 시작 로직
            // ...

            CommonRoomResponse response = new CommonRoomResponse(roomId, "HIDE_START", null, roomInfo);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, response);
        } catch (Exception e) {
            log.error("Error starting hide phase: ", e);
        }
    }

    // 다른 메소드들 (findStart, seekerWin, hiderWin, backRoom 등) 추가
    // ...

}