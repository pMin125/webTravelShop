package com.toyProject.service;

import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@RequiredArgsConstructor
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserEntityRepository userEntityRepository;


    @Transactional
    public void addToCart(@AuthenticationPrincipal UserEntity user, Long productId, int quantity) {
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = createNewCart(user);
                    System.out.println("새 장바구니가 생성되었습니다.");
                    return newCart;
                });

        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            System.out.println("상품이 DB에 존재하지 않습니다.");
            throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
        }
        Product product = productOptional.get();
        System.out.println("상품 조회 성공: " + product);

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
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cart.getCartItems().add(cartItem);
            System.out.println("새 CartItem 추가됨");
        }

        cartRepository.save(cart);
    }

    private Cart createNewCart(UserEntity user) {
        Cart newCart = new Cart();
        newCart.setUser(user);
        return cartRepository.save(newCart);
    }

    @Transactional
    public void removeFromCart(UserEntity user, Long productId) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 장바구니를 찾을 수 없습니다."));

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cart.getCartItems().remove(cartItem);
        } else {
            throw new IllegalArgumentException("장바구니에 해당 상품이 존재하지 않습니다.");
        }
        cartRepository.save(cart);
    }
}
