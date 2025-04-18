package com.toyProject.controller;

import com.toyProject.dto.ChatMessage;
import com.toyProject.entity.Chat;
import com.toyProject.service.ChatService;
import com.toyProject.service.OrdrService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage message) {
        // DB 저장
        chatService.sendMessage(message);

        // 채팅방별로 메시지 전송
        messagingTemplate.convertAndSend("/topic/chat/" + message.getRoomId(), message);
    }

    @GetMapping("/chat/{roomId}")
    public List<Chat> getChatHistory(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return chatService.getMessagesByRoomIdV2(roomId, page, size);
    }
}