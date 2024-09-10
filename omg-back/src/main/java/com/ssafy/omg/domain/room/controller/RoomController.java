package com.ssafy.omg.domain.room.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.ssafy.omg.domain.room.dto.CommonRoomRequest;
import com.ssafy.omg.domain.room.dto.CommonRoomResponse;
import com.ssafy.omg.domain.room.service.RoomService;

@Controller
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @MessageMapping("/room/enter")
    @SendTo("/pub/room")
    public CommonRoomResponse enterRoom(CommonRoomRequest request) {
        return roomService.enterRoom(request);
    }

    @MessageMapping("/room/leave")
    @SendTo("/pub/room")
    public CommonRoomResponse leaveRoom(CommonRoomRequest request) {
        return roomService.leaveRoom(request);
    }

    @MessageMapping("/room/start")
    @SendTo("/pub/room")
    public CommonRoomResponse startButtonClicked(CommonRoomRequest request) {
        return roomService.startButtonClicked(request);
    }

    @MessageMapping("/room/rendered")
    @SendTo("/pub/room")
    public CommonRoomResponse renderedComplete(CommonRoomRequest request) {
        return roomService.renderedComplete(request);
    }
}