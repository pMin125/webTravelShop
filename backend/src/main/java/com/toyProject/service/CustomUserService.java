package com.toyProject.service;

import com.toyProject.dto.SignUpUser;
import com.toyProject.entity.UserEntity;
import com.toyProject.repository.UserEntityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CustomUserService implements UserDetailsService {

    private final UserEntityRepository userEntityRepository;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수  없습니다."));
        user.getRoles().size();
        System.out.println("유저 찾음: " + user.getUsername());
        System.out.println("DB 비밀번호: " + user.getPassword());
        return user;
    }
}
