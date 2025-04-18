package com.toyProject.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.siot.IamportRestClient.IamportClient;
import com.toyProject.dto.RequestPayDTO;
import com.toyProject.entity.Order;
import com.toyProject.entity.OrderItem;
import com.toyProject.entity.Payment;
import com.toyProject.repository.CartRepository;
import com.toyProject.repository.OrderRepository;
import com.toyProject.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final IamportClient iamportClient;

    public Payment processPayment(String paymentUid, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        Payment payment = Payment.builder()
                .paymentUid(paymentUid)
                .price((long) order.getTotalPrice())
                .status(Payment.PaymentStatus.SUCCESS)
                .build();

        order.setPayment(payment);
        orderRepository.save(order);

        return paymentRepository.save(payment);
    }

    public RequestPayDTO findRequestDto(String orderUid) {

        Order order = orderRepository.findOrderAndPaymentAndMember(orderUid)
                .orElseThrow(() -> new IllegalArgumentException("주문이 없습니다."));

//        Cart cart = cartRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new IllegalArgumentException("장바구니에 상품이 존재하지 않습니다."));

        List<OrderItem> items = order.getOrderItems();
        String itemName;
        if (items.size() == 1) {
            itemName = items.get(0).getProduct().getProductName();
        } else if (items.size() > 1) {
            itemName = items.get(0).getProduct().getProductName() + " 외 " + (items.size() - 1) + "개";
        } else {
            itemName = "";
        }

        return RequestPayDTO.builder()
                .buyerName(order.getUser().getUsername())
                .buyerEmail(order.getUser().getEmail())
                .buyerAddress("address")
                .paymentPrice(order.getTotalPrice())
                .itemName(itemName)
                .orderUid(order.getOrderUid())
                .build();
    }

}
