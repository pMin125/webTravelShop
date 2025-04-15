package com.toyProject.service;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.toyProject.dto.PaymentCallbackDTO;
import com.toyProject.dto.Request.OrderRequest;
import com.toyProject.entity.*;
import com.toyProject.exception.ParticipationException;
import com.toyProject.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

import static com.toyProject.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdrService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserEntityRepository userEntityRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final IamportClient iamportClient;
    private final ParticipationRepository participationRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public void handleExpiredPayment(String key) {
        // Key 형식: payment:expire:{productId}:{username}
        try {
            String[] parts = key.split(":");
            Long productId = Long.parseLong(parts[2]);
            String username = parts[3];

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("상품 없음"));
            UserEntity user = userEntityRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("유저 없음"));

            // 참여 상태 CANCELLED 로 변경
            participationRepository.findActiveParticipationByUserAndProduct(user, product)
                    .ifPresent(participation -> {
                        participation.setStatus(Participation.ParticipationStatus.CANCELLED);
                        participationRepository.save(participation);
                        System.out.print("❌ 결제 미완료로 참여 취소됨: {}"+ username);
                    });

            // Redis에서 키 삭제
            redisTemplate.delete(key);

            // 다음 대기자 알림 로직 호출
            notifyNextUserInQueue(productId);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("오류");
        }
    }
    public ResponseEntity<Order> addOrder(UserEntity user, OrderRequest orderRequest) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 비어있습니다."));

        // 2. 선택된 상품들에 대해 기존의 `PENDING` 상태 주문을 취소
        List<Long> selectedProductIds = orderRequest.getSelectedProductIds();
        for (Long productId : selectedProductIds) {
            // 해당 상품에 대한 기존 `PENDING` 주문을 취소
            Optional<Order> existingOrder = orderRepository.findTopOrderByUserAndProductAndStatus(
                    user, productRepository.findById(productId).orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다.")),
                    Order.OrderStatus.PENDING
            );

            if (existingOrder.isPresent()) {
                Order pendingOrder = existingOrder.get();
                pendingOrder.setOrderStatus(Order.OrderStatus.CANCELED);  // 기존 주문 취소
                Payment payment = pendingOrder.getPayment();
                if (payment != null) {
                    payment.setStatus(Payment.PaymentStatus.CANCELLED);  // 결제도 취소 상태로 변경
                    paymentRepository.save(payment);
                }
                orderRepository.save(pendingOrder);
            }
        }

        Order order = Order.builder()
                .user(user)
                .orderUid(generateOrderUid())
                .orderStatus(Order.OrderStatus.PENDING)
                .build();

        Payment payment = Payment.builder()
                .price(cart.getTotalPrice())
                .build();
        order.setPayment(payment);

        if (order.getOrderItems() == null) {
            order.setOrderItems(new ArrayList<>());
        }

        for (CartItem cartItem : cart.getCartItems()) {
            if (selectedProductIds.contains(cartItem.getProduct().getId())) {
                OrderItem orderItem = OrderItem.builder()
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .order(order)
                        .build();
                order.getOrderItems().add(orderItem);
            }
        }

        orderRepository.save(order);

        // 6. 장바구니 비우기 (필요한 경우)
//        cart.getCartItems().clear();
//        cartRepository.save(cart);

        return ResponseEntity.ok(order);
    }


    @Transactional
    public Order createSingleOrder(UserEntity user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));

        Optional<Order> existingOrder = orderRepository.findTopOrderByUserAndProductAndStatus(
                user, product, Order.OrderStatus.PENDING
        );

        if (existingOrder.isPresent()) {
            return existingOrder.get(); // 기존 걸 그대로 씀
        }
        Order order = Order.builder()
                .user(user)
                .orderUid(generateOrderUid())
                .orderStatus(Order.OrderStatus.PENDING)
                .build();

        Payment payment = Payment.builder()
                .price((long) product.getPrice())
                .build();
        order.setPayment(payment);

        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(1)
                .order(order)
                .build();
        order.setOrderItems(Collections.singletonList(orderItem));

        orderRepository.save(order);
        return order;
    }

    public IamportResponse<com.siot.IamportRestClient.response.Payment> paymentByCallback(PaymentCallbackDTO request) {
        try {
            IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = iamportClient.paymentByImpUid(request.getPaymentUid());

            Order order = orderRepository.findOrderAndPayment(request.getOrderUid())
                    .orElseThrow(() -> new IllegalArgumentException("주문 내역이 없습니다."));

            if (!iamportResponse.getResponse().getStatus().equals("paid")) {
                orderRepository.delete(order);
                paymentRepository.delete(order.getPayment());
                throw new RuntimeException("결제 미완료");
            }

            Long price = order.getPayment().getPrice();
            int iamportPrice = iamportResponse.getResponse().getAmount().intValue();
            System.out.print("1appleappleappleapple");
            log.info("iamportPriceiamportPriceiamportPrice : ",iamportPrice);
            log.info("pricepricepricepricepricepriceprice : ",price);
            if (iamportPrice != price) {
                orderRepository.delete(order);
                paymentRepository.delete(order.getPayment());
                iamportClient.cancelPaymentByImpUid(new CancelData(iamportResponse.getResponse().getImpUid(), true, new BigDecimal(iamportPrice)));

                throw new RuntimeException("결제금액 위변조 의심");
            }
            order.setOrderStatus(Order.OrderStatus.SUCCESS);
            System.out.print("appleappleappleapple");
            order.getPayment().changePaymentBySuccess(Payment.PaymentStatus.SUCCESS, iamportResponse.getResponse().getImpUid());
            orderRepository.save(order);
            paymentRepository.save(order.getPayment());
            System.out.print(iamportResponse.getResponse().getImpUid());
            return iamportResponse;

        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException("결제 처리 중 오류가 발생했습니다.", e);
        }
    }
    @Transactional
    public void cancelPayment(UserEntity user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ParticipationException(PRODUCT_NOT_FOUND));

        Order order = orderRepository.findTopOrderByUserAndProductAndStatus(user, product, Order.OrderStatus.SUCCESS)
                .orElseThrow(() -> new ParticipationException(ORDER_NOT_FOUND));

        Payment payment = order.getPayment();
        if (payment == null || payment.getPaymentUid() == null) {
            throw new ParticipationException(PAYMENT_NOT_FOUND);
        }

        // 1. iamport 결제 취소 요청
        try {
            CancelData cancelData = new CancelData(payment.getPaymentUid(), true);
            iamportClient.cancelPaymentByImpUid(cancelData);
        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException("결제 취소 실패", e);
        }

        // 2. 상태 업데이트
        order.setOrderStatus(Order.OrderStatus.CANCELED);
        payment.changePaymentStatus(Payment.PaymentStatus.CANCELLED);
        orderRepository.save(order);
        paymentRepository.save(payment);

        // 3. Participation도 취소
        Participation participation = participationRepository.findActiveParticipationByUserAndProduct(user, product)
                .orElseThrow(() -> new ParticipationException(PARTICIPATION_NOT_FOUND));

        participation.setStatus(Participation.ParticipationStatus.CANCELLED);
        participationRepository.save(participation);

        notifyNextUserInQueue(productId);
    }

    @Transactional
    public void notifyNextUserInQueue(Long productId) {
        log.info("🔔 notifyNextUserInQueue 진입: productId={}", productId);

        String listKey = "queue:product:" + productId;
        String setKey = listKey + ":waitingSet";

        String nextUsername = redisTemplate.opsForList().leftPop(listKey);
        if (nextUsername == null) {
            log.info("❌ 대기열에 유저 없음");
            return;
        }

        redisTemplate.opsForSet().remove(setKey, nextUsername);

        UserEntity user = userEntityRepository.findByUsername(nextUsername)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다: " + nextUsername));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품 없음"));
        Participation participation = participationRepository.findActiveParticipationByUserAndProduct(user, product)
                .orElseThrow(() -> new RuntimeException("참여 내역 없음"));

        participation.setStatus(Participation.ParticipationStatus.WAITING_PAYMENT);

        // 소켓 알림 로그 추가
        log.info("📢 알림 보낼 유저: {} / 상품: {}", user.getUsername(), productId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", productId);
        payload.put("message", "대기 순번이 되었습니다! 30분 안에 결제하세요.");
        payload.put("sender", user.getUsername()); // ✅ 필터용 sender 추가
        payload.put("type", "WAITING_NOTIFY");     // ✅ 타입도 명시 (선택사항)

        log.info("📨 convertAndSend (브로드캐스트) 호출 시작");

        messagingTemplate.convertAndSend(
                "/sub/notify/" + productId, // ✅ 방 기반 알림 채널
                payload
        );

        String ttlKey = "payment:expire:" + productId + ":" + user.getUsername();
        redisTemplate.opsForValue().set(ttlKey, "waiting", Duration.ofMinutes(1));
        log.info("⏱️ TTL 설정 완료: {}", ttlKey);
    }


    private String generateOrderUid() {
        return UUID.randomUUID().toString();
    }

    public List<Product> orderProduct(UserEntity user) {
        List<Order> orders = orderRepository.findByUser(user);
        List<Product> result = new ArrayList<>();

        for (Order order : orders) {
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem item : orderItems) {
                result.add(item.getProduct());
            }
        }
        return result;
    }
}
