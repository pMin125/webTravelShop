package com.toyProject.service;


import com.toyProject.dto.SignUpUser;
import com.toyProject.entity.UserEntity;
import com.toyProject.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserEntityRepository userEntityRepository;
    private final PasswordEncoder passwordEncoder;
    public UserEntity createUser(SignUpUser signUpUser) {
        UserEntity user = UserEntity.builder()
                .email(signUpUser.getEmail())
                .username(signUpUser.getUsername())
                .nickName(signUpUser.getNickName())
                .password(passwordEncoder.encode(signUpUser.getPassword()))
                .build();
        System.out.print(signUpUser.getPassword());
        return userEntityRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userEntityRepository.deleteById(userId);
    }

    public List<UserEntity> getUsers() {
        return userEntityRepository.findAll();
    }

}