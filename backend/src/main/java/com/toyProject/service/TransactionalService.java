package com.toyProject.service;

import static com.toyProject.entity.Participation.ParticipationStatus.JOINED;
import static com.toyProject.entity.Participation.ParticipationStatus.WAITING_PAYMENT;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyProject.entity.Chat;
import com.toyProject.entity.Order;
import com.toyProject.entity.Participation;
import com.toyProject.entity.Product;
import com.toyProject.entity.UserEntity;
import com.toyProject.exception.ErrorCode;
import com.toyProject.exception.ParticipationException;
import com.toyProject.repository.OrderRepository;
import com.toyProject.repository.ParticipationRepository;
import com.toyProject.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionalService {

    private final ProductRepository productRepository;
    private final ParticipationRepository participationRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;
    @Transactional
    public Participation.ParticipationStatus doParticipate(UserEntity user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ParticipationException(ErrorCode.PRODUCT_NOT_FOUND));

        Optional<Participation> participations = participationRepository.findActiveParticipationByUserAndProduct(user, product);

        participations.stream()
                .filter(p -> p.getStatus() != Participation.ParticipationStatus.CANCELLED)
                .findFirst()
                .ifPresent(prev -> {
                    switch (prev.getStatus()) {
                        case WAITING_PAYMENT -> throw new ParticipationException(ErrorCode.ALREADY_WAITING_PAYMENT);
                        case JOINED -> throw new ParticipationException(ErrorCode.ALREADY_JOINED);
                        case WAITING_LIST -> throw new ParticipationException(ErrorCode.ALREADY_IN_WAITING_LIST);
                    }
                });

        int joinedCount = participationRepository.countByProductAndStatus(product, Participation.ParticipationStatus.JOINED);
        Participation.ParticipationStatus status = (joinedCount < product.getCapacity())
                ? Participation.ParticipationStatus.WAITING_PAYMENT
                : Participation.ParticipationStatus.WAITING_LIST;

        Participation participation = Participation.builder()
                .user(user)
                .product(product)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        participationRepository.save(participation);

        if (status == Participation.ParticipationStatus.WAITING_LIST) {
            String listKey = "queue:product:" + productId;
            String setKey = listKey + ":waitingSet";

            Long isAdded = redisTemplate.opsForSet().add(setKey, user.getUsername());
            if (isAdded != null && isAdded > 0) {
                redisTemplate.opsForList().rightPush(listKey, user.getUsername());
            }
        }

        return status;
    }

    @Transactional
    public void confirmPayment(UserEntity user, Long productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));

            Participation participation = participationRepository
                    .findActiveParticipationByUserAndProduct(user, product)
                    .orElseThrow(() -> new RuntimeException("참여 내역이 없습니다."));

            if (participation.getStatus() != WAITING_PAYMENT) {
                throw new IllegalStateException("결제 대기 상태가 아닙니다.");
            }

            Order order = orderRepository.findTopOrderByUserAndProductAndStatus(user, product, Order.OrderStatus.SUCCESS)
                    .orElseThrow(() -> new RuntimeException("결제 내역이 존재하지 않거나 결제가 완료되지 않았습니다."));

            participation.setStatus(JOINED);
            participation.setCreatedAt(LocalDateTime.now());
            participationRepository.save(participation);
            
            Chat message = Chat.builder()
                    .roomId(productId.toString())
                    .sender("system")
                    .message("참여 인원이 변경되었습니다.")
                    .type(Chat.MessageType.UPDATE)
                    .build();

            messagingTemplate.convertAndSend("/sub/notify/" + productId, message);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

}
