package com.toyProject.repository;


import com.toyProject.entity.Cart;
import com.toyProject.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(UserEntity user);

    Optional<Cart> findByUser_UserId(Long userId);

    @Query("SELECT c FROM Cart c " +
            "LEFT JOIN FETCH c.cartItems ci " +
            "LEFT JOIN FETCH ci.product " +
            "WHERE c.user.userId = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {
            "cartItems",
            "cartItems.product",
            "cartItems.product.tags"
    })
    @Query("SELECT c FROM Cart c WHERE c.user.userId = :userId")
    Optional<Cart> findByUserIdWithDetails(@Param("userId") Long userId);
}