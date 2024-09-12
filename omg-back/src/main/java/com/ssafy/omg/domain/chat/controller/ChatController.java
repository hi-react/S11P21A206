package com.ssafy.omg.domain.chat.controller;

import com.ssafy.omg.domain.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/rooms/{roodId}")
    public void chat(@DestinationVariable String roomId, ChatMessage chatMessage) {
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, chatMessage);
    }
}
