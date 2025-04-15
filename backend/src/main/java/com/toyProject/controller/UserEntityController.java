package com.toyProject.controller;

import com.toyProject.dto.LoginUser;
import com.toyProject.dto.SignUpUser;
import com.toyProject.entity.Cart;
import com.toyProject.entity.UserEntity;
import com.toyProject.repository.CartRepository;
import com.toyProject.repository.UserEntityRepository;
import com.toyProject.service.CustomUserService;
import com.toyProject.service.UserService;
import com.toyProject.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class UserEntityController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserService customUserService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserEntityRepository userEntityRepository;
    private final CartRepository cartRepository;

    // 회원등록
    @PostMapping("/signUp")
    public ResponseEntity<UserEntity> createUser(@RequestBody SignUpUser signUpUser) {
        UserEntity user = userService.createUser(signUpUser);
        return ResponseEntity.ok(user);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUser loginUser,
                                        HttpServletResponse response) throws AuthenticationException {
        try {

            System.out.println("입력한 username: " + loginUser.getUsername());
            System.out.println("입력한 password: " + loginUser.getPassword());

            UserDetails userDetails = customUserService.loadUserByUsername(loginUser.getUsername());
            System.out.println("DB 비밀번호: " + userDetails.getPassword());

            boolean matches = passwordEncoder.matches(loginUser.getPassword(), userDetails.getPassword());
            System.out.println("패스워드 일치?: " + matches);

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword())
            );

            String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());

            Cookie cookie = new Cookie("onion_token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60);
            cookie.setSecure(true);

            response.addCookie(cookie);

            UserEntity user = userEntityRepository.findByUsername(loginUser.getUsername()).orElseThrow();
            String nickname = user.getNickName();
            String name = user.getUsername();
            // 7. 토큰 + 닉네임 같이 응답
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "nickname", nickname,
                    "name", name
            ));
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
        }
    }

    // 하트 기능
    @GetMapping("/heart/me")
    public ResponseEntity<?> getHeartStatus(@AuthenticationPrincipal UserEntity user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인하지 않았습니다.");
        }

        Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(user.getUserId());

        if (cartOpt.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        Cart cart = cartOpt.get();
        List<Long> heartProductIds = cart.getCartItems().stream()
                .map(cartItem -> cartItem.getProduct().getId())
                .collect(Collectors.toList());

        return ResponseEntity.ok(heartProductIds);
    }

    // 프론트 로그인 여부
    @GetMapping("/auth/me")
    public ResponseEntity<?> checkLogin(@AuthenticationPrincipal UserEntity user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인하지 않았습니다.");
        }

        return ResponseEntity.ok(Map.of(
                "id", user.getUserId()
        ));
    }
}
