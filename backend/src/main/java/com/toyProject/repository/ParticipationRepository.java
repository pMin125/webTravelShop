package com.toyProject.repository;

import com.toyProject.entity.Participation;
import com.toyProject.entity.Product;
import com.toyProject.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    int countByProductAndStatus(Product product, Participation.ParticipationStatus participationStatus);

    @Query("SELECT p.user FROM Participation p WHERE p.product.id = :productId AND p.status = 'JOINED'")
    List<UserEntity> findUsersByProductIdAndStatus(@Param("productId") Long productId, Participation.ParticipationStatus joined);

    Optional<Participation> findByUserAndProduct(UserEntity user, Product product);

    @Query("SELECT p FROM Participation p WHERE p.user = :user AND p.product = :product AND p.status <> 'CANCELLED'")
    Optional<Participation> findActiveParticipationByUserAndProduct(@Param("user") UserEntity user,
                                                                    @Param("product") Product product);

    @Query("SELECT p.product.id, COUNT(p.id) " +
            "FROM Participation p " +
            "WHERE p.status = :status " +
            "GROUP BY p.product.id")
    List<Object[]> countGroupByProduct(@Param("status") Participation.ParticipationStatus status);
}
