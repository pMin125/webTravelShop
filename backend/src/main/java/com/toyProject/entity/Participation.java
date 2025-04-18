package com.toyProject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "participation_table")
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @ManyToOne
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ParticipationStatus status;

    private LocalDateTime createdAt;

    public enum ParticipationStatus {
        JOINED,
        WAITING_PAYMENT, // 입급전
        CANCELLED, // 결제 취소
        WAITING_LIST // 대기
    }
}
