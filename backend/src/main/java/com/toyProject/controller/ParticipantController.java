package com.toyProject.controller;

import com.toyProject.entity.Order;
import com.toyProject.entity.Participation;
import com.toyProject.entity.UserEntity;
import com.toyProject.exception.ParticipationException;
import com.toyProject.service.OrdrService;
import com.toyProject.service.ParticipantService;
import com.toyProject.service.ProductService;
import com.toyProject.service.TransactionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/participant")
public class ParticipantController {

    private final ParticipantService participationService;
    private final TransactionalService transactionalService;

    // 참가
    @PostMapping("/{productId}")
    public ResponseEntity<?> participate(@PathVariable Long productId, @AuthenticationPrincipal UserEntity user) {
        try {
            Participation.ParticipationStatus status = participationService.participate(user, productId);
            return ResponseEntity.ok(Map.of("status", status.name()));
        } catch (ParticipationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getErrorCode().name(),
                    "message", e.getMessage()
            ));
        }
    }

    // 결제후 상태값 변경
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@AuthenticationPrincipal UserEntity user,
                                            @RequestParam Long productId) {
        try {
            transactionalService.confirmPayment(user, productId);
            return ResponseEntity.ok("참여 확정이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("참여 확정 실패: " + e.getMessage());
        }
    }

    // 통계 + JOIN
    @GetMapping("/summary/{productId}")
    public ResponseEntity<Map<String, Object>> getParticipationSummary(
            @PathVariable Long productId
    ) {
        Map<String, Object> summary = participationService.getSummaryForProduct(productId);
        return ResponseEntity.ok(summary);
    }

    // 참가 상태
    @GetMapping("/status/{productId}")
    public ResponseEntity<Map<String, String>> getParticipationStatus(@PathVariable Long productId, @AuthenticationPrincipal UserEntity user) {
        Map<String, String> result = participationService.getParticipationStatusMap(productId, user);
        return ResponseEntity.ok(result);
    }

    // 대기
    @DeleteMapping("/cancel/{productId}")
    public ResponseEntity<?> cancelParticipation(@PathVariable Long productId, @AuthenticationPrincipal UserEntity user) {
        participationService.cancelWaiting(user, productId);
        return ResponseEntity.ok("대기 취소 완료되었습니다.");
    }

    // 참여 취소 대기자 JOIN
    @DeleteMapping("/cancel/join/{productId}")
    public ResponseEntity<?> cancelJoinParticipation(@PathVariable Long productId, @AuthenticationPrincipal UserEntity user) {
        participationService.cancelJoin(user, productId);
        return ResponseEntity.ok("참여 취소 및 대기자 join 완료되었습니다.");
    }

    // 결제 시간 REDIS
    @GetMapping("/payment-ttl")
    public ResponseEntity<?> getPaymentTTL(@AuthenticationPrincipal UserEntity user, @RequestParam Long productId) {
        Long ttl = participationService.getPaymentTTL(user, productId);
        return ResponseEntity.ok(Map.of("remainingSeconds", ttl));
    }

}
