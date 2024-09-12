package com.ssafy.omg.domain.socket.controller;

import com.ssafy.omg.domain.room.dto.CommonRoomRequest;
import com.ssafy.omg.domain.socket.service.RoomSocketServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RoomSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomSocketServiceImpl roomSocketService;

    @MessageMapping("/room.modify")
    public void roomModify(@Payload CommonRoomRequest request) {
        log.info("Received room modify request: {}", request);
        roomSocketService.modifyRoom(request);
    }

    @MessageMapping("/room.gameInit")
    public void gameInit(@Payload CommonRoomRequest request) {
        log.info("Received game init request: {}", request);
        roomSocketService.gameInit(request);
    }

    @MessageMapping("/room.hideStart")
    public void hideStart(@Payload CommonRoomRequest request) {
        log.info("Received hide start request: {}", request);
        roomSocketService.hideStart(request.getRoomId());
    }

    @MessageMapping("/room.findStart")
    public void findStart(@Payload CommonRoomRequest request) {
        log.info("Received find start request: {}", request);
        // Implement findStart logic in RoomSocketServiceImpl and call it here
    }

    @MessageMapping("/room.seekerWin")
    public void seekerWin(@Payload CommonRoomRequest request) {
        log.info("Received seeker win request: {}", request);
        // Implement seekerWin logic in RoomSocketServiceImpl and call it here
    }

    @MessageMapping("/room.hiderWin")
    public void hiderWin(@Payload CommonRoomRequest request) {
        log.info("Received hider win request: {}", request);
        // Implement hiderWin logic in RoomSocketServiceImpl and call it here
    }

    @MessageMapping("/room.backRoom")
    public void backRoom(@Payload CommonRoomRequest request) {
        log.info("Received back room request: {}", request);
        // Implement backRoom logic in RoomSocketServiceImpl and call it here
    }

    // Add more message mappings as needed
}