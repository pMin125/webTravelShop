package com.toyProject.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_table")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentUid;

    private Long price;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @OneToOne(mappedBy = "payment")
    @JsonBackReference
    private Order order;

    public void changePaymentBySuccess(PaymentStatus status, String paymentUid) {
        this.status = status;
        this.paymentUid = paymentUid;
    }

    public void changePaymentStatus(PaymentStatus status) {
        this.status = status;
    }
    public enum PaymentStatus {
        PENDING,  // 결제 대기 중
        SUCCESS,  // 결제 성공
        FAILED,   // 결제 실패
        CANCELLED // 결제 취소
    }
}

