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
// 참여 -> 참여버튼 -> 결제페이지 -> 결제 취소햇을경우 -> 상태값 cancelled 로 변경 -> 결제햇을경우 -> JOINED 로 변경

// 대기 -> 대기버튼 -> 소켓에따른 실시간 알림 -> 30분내입금(상태값 wating_payment) -> 입금 안햇을경우 cancel  -> 다음대기 사람 반복