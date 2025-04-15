package com.toyProject.repository;


import com.toyProject.entity.Cart;
import com.toyProject.entity.Tag;
import com.toyProject.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}