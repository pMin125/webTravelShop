package com.toyProject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId; // 상품 ID
    private String sender;
    private String message;
    private MessageType type;
    private LocalDateTime sentAt;

    public enum MessageType {
        ENTER, TALK, UPGRADE, UPDATE
    }
}
