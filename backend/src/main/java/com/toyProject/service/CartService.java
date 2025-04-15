package com.toyProject.service;

import com.toyProject.entity.Cart;
import com.toyProject.entity.CartItem;
import com.toyProject.entity.Product;
import com.toyProject.entity.UserEntity;
import com.toyProject.exception.ErrorCode;
import com.toyProject.exception.ParticipationException;
import com.toyProject.repository.CartRepository;
import com.toyProject.repository.ProductRepository;
import com.toyProject.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserEntityRepository userEntityRepository;


    @Transactional
    public void addToCart(@AuthenticationPrincipal UserEntity user, Long productId, int quantity) {
        // 1. 사용자에 해당하는 장바구니 조회 또는 생성
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = createNewCart(user);
                    System.out.println("새 장바구니가 생성되었습니다.");
                    return newCart;
                });

        // 2. 상품 조회
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            System.out.println("상품이 DB에 존재하지 않습니다.");
            throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
        }
        Product product = productOptional.get();
        System.out.println("상품 조회 성공: " + product);

        // 3. 장바구니 내에 해당 상품이 이미 있는지 확인
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            throw new ParticipationException(ErrorCode.ALREADY_ADD_PRODUCT);
            // 이미 존재하면 수량 업데이트
//            CartItem cartItem = existingItem.get();
//            cartItem.setQuantity(cartItem.getQuantity() + quantity);
//            System.out.println("기존 상품 수량 업데이트: " + cartItem.getQuantity());
        } else {
            // 존재하지 않으면 새 CartItem 생성
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cart.getCartItems().add(cartItem);
            System.out.println("새 CartItem 추가됨");
        }

        // 4. 장바구니 저장 (CascadeType.ALL로 인해 CartItem도 함께 저장됨)
        cartRepository.save(cart);
    }

    // 새 장바구니 생성 메서드
    private Cart createNewCart(UserEntity user) {
        Cart newCart = new Cart();
        newCart.setUser(user);
        return cartRepository.save(newCart);
    }

    @Transactional
    public void removeFromCart(UserEntity user, Long productId) {
        // 사용자에 해당하는 장바구니 조회
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 장바구니를 찾을 수 없습니다."));

        // 장바구니 내에 해당 상품이 존재하는지 확인
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // 존재하면 해당 CartItem 제거
            CartItem cartItem = existingItem.get();
            cart.getCartItems().remove(cartItem);
            System.out.println("CartItem 제거됨: " + cartItem.getProduct().getProductName());
        } else {
            // 존재하지 않으면 예외 처리
            throw new IllegalArgumentException("장바구니에 해당 상품이 존재하지 않습니다.");
        }

        // 변경된 Cart 저장 (orphanRemoval=true로 인해 CartItem도 삭제됨)
        cartRepository.save(cart);
    }
}
