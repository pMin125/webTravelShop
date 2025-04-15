package com.toyProject.repository;


import com.toyProject.entity.Cart;
import com.toyProject.entity.Chat;
import com.toyProject.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByRoomIdOrderBySentAtAsc(String roomId);

    @Query("SELECT c FROM Chat c WHERE c.roomId = :roomId")
    Page<Chat> findByRoomId(@Param("roomId") String roomId, Pageable pageable);
}