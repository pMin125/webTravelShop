package com.toyProject.controller;

import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.toyProject.dto.PaymentCallbackDTO;
import com.toyProject.dto.Request.OrderRequest;
import com.toyProject.entity.Order;
import com.toyProject.entity.Product;
import com.toyProject.entity.UserEntity;
import com.toyProject.service.OrdrService;
import com.toyProject.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ordr")
public class OrdrController {

    private final ProductService productService;
    private final OrdrService ordrService;

    //장바구니 주문
    @PostMapping("/createOrder")
    public ResponseEntity<Order> addOrder(@AuthenticationPrincipal UserEntity user, @RequestBody OrderRequest orderRequest) {
       return ordrService.addOrder(user,orderRequest);
    }

    //단일 상품 주문
    @PostMapping("/single")
    public ResponseEntity<Order> addSingleOrder(@AuthenticationPrincipal UserEntity user, @RequestParam Long productId) {
        Order order = ordrService.createSingleOrder(user, productId);
        return ResponseEntity.ok(order);
    }

    //결제 후
    @ResponseBody
    @PostMapping("/payment")
    public ResponseEntity<IamportResponse<Payment>> validationPayment(@AuthenticationPrincipal UserEntity user, @RequestBody PaymentCallbackDTO request) {
        IamportResponse<Payment> iamportResponse = ordrService.paymentByCallback(request);
        return new ResponseEntity<>(iamportResponse, HttpStatus.OK);
    }

    //취소
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPayment(@AuthenticationPrincipal UserEntity user,
                                           @RequestParam Long productId) {
        try {
            ordrService.cancelPayment(user, productId);
            return ResponseEntity.ok("참여 취소 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("취소 실패: " + e.getMessage());
        }
    }

    @GetMapping("/orderProduct")
    public ResponseEntity<List<Product>> orderProduct(@AuthenticationPrincipal UserEntity user) {
        try {
            List<Product> products = ordrService.orderProduct(user);
            return ResponseEntity.ok(products);  // 실제 상품 리스트 반환
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();  // 실패 시 400 응답
        }
    }
}
