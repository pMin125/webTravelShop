package com.toyProject.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyProject.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public void handleMessage(String messageJson) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(messageJson, ChatMessage.class);
            System.out.println("Redis에서 받은 메시지: " + chatMessage.getMessage());
            String roomId = chatMessage.getRoomId();

            if ("UPGRADE".equals(chatMessage.getType())) {
                messagingTemplate.convertAndSend("/sub/notify/" + roomId, chatMessage);
            } else {
                messagingTemplate.convertAndSend("/sub/chat/" + roomId, chatMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
