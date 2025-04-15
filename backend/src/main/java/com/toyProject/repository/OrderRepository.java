package com.toyProject.repository;

import com.toyProject.entity.Order;
import com.toyProject.entity.Product;
import com.toyProject.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("select o from Order o left join fetch o.payment join fetch o.user where o.orderUid = :orderUid")
    Optional<Order> findOrderAndPaymentAndMember(@Param("orderUid") String orderUid);
    @Query("select o from Order o" +
            " left join fetch o.payment p" +
            " where o.orderUid = :orderUid")
    Optional<Order> findOrderAndPayment(String orderUid);

    @Query("SELECT o FROM Order o " +
            "JOIN o.orderItems oi " +
            "WHERE o.user = :user AND oi.product = :product AND o.orderStatus = :status " +
            "ORDER BY o.createdDate DESC")
    Optional<Order> findTopOrderByUserAndProductAndStatus(
            @Param("user") UserEntity user,
            @Param("product") Product product,
            @Param("status") Order.OrderStatus status
    );

    List<Order> findByUser(UserEntity user);
}
