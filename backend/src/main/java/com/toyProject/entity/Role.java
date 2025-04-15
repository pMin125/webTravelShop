package com.toyProject.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // 예: ROLE_USER, ROLE_ADMIN

    @ManyToMany(mappedBy = "roles")
    private Set<UserEntity> users;
}
