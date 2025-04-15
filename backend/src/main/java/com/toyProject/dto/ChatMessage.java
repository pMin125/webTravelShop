package com.toyProject.dto;

import lombok.*;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    public enum MessageType {
        ENTER, TALK
    }

    private MessageType type;   // ENTER, TALK
    private String roomId;      // 어떤 여행 상품 채팅방인지 (상품 ID)
    private String sender;      // 보낸 사람
    private String message;     // 메시지 내용
}
