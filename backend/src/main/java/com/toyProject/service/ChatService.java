package com.toyProject.service;

import com.toyProject.dto.ChatMessage;
import com.toyProject.entity.Chat;
import com.toyProject.entity.ChatMessageEntity;
import com.toyProject.entity.Product;
import com.toyProject.entity.UserEntity;
import com.toyProject.repository.ChatRepository;
import com.toyProject.repository.ProductRepository;
import com.toyProject.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRepository chatRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${my.server-id}")
    private String serverId;
    public void sendMessage(ChatMessage chatMessage) {
        log.info("[{}] 서버에서 메시지 전송: {}", serverId, chatMessage.getMessage());
        Chat chat = Chat.builder()
                .roomId(chatMessage.getRoomId())
                .sender(chatMessage.getSender())
                .message(chatMessage.getMessage())
                .sentAt(LocalDateTime.now())
                .build();

        chatRepository.save(chat);

        String topic = "chat:" + chatMessage.getRoomId();
        redisTemplate.convertAndSend(topic, chatMessage);

        if ("UPGRADE".equals(chatMessage.getType())) {
            // TOAST알림용
            messagingTemplate.convertAndSend(
                    "/sub/notify/" + chatMessage.getRoomId(),
                    chatMessage
            );
        }
        // 모든사람 알림
        if ("UPDATE".equals(chatMessage.getType())) {
            messagingTemplate.convertAndSend(
                    "/sub/notify/" + chatMessage.getRoomId(),
                    chatMessage
            );
        }
    }

    public List<Chat> getMessagesByRoomId(String roomId) {
        return chatRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }

    public List<Chat> getMessagesByRoomIdV2(String roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<Chat> chatPage = chatRepository.findByRoomId(roomId, pageable);
        return chatPage.getContent();
    }
}
