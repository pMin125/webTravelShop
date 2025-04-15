package com.toyProject.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyProject.service.CustomUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Collection;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {
    @Autowired
    private CustomUserService customUserService;

    static ObjectMapper objectMapper;
    private final String secretKey = "LiLWe5swxwvcuUCUXIvOG6crppxJUgTyAczWRKSvkDo=";  // Access Token Secret Key
    private long expirationTime = 3600000;

    public Authentication getAuthentication(String token) {
        log.info("🪙 WebSocket 토큰: {}", token);

        try {
            String username = getUsernameFromToken(token);
            log.info("👤 토큰에서 추출한 username: {}", username);

            UserDetails userDetails = customUserService.loadUserByUsername(username);
            log.info("✅ userDetails 조회 완료: {}", userDetails.getUsername());

            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        } catch (Exception e) {
            log.error("❌ getAuthentication 실패: {}", e.getMessage());
            return null;
        }
    }


    public String generateToken(String username, Collection<? extends GrantedAuthority> roles) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(username)  // 사용자 이름
                .claim("roles", roles)  // 권한 정보
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            System.out.println("Validating Token: " + token);
            System.out.println("Secret Key: " + secretKey);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Base64.getDecoder().decode(secretKey))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // ✅ 만료 시간 로그 확인
            Date expiration = claims.getExpiration();
            System.out.println("토큰 만료 시간: " + expiration);
            System.out.println("현재 시간: " + new Date());

            return !expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            System.out.println("❌ 토큰이 만료되었습니다: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ 토큰 검증 실패: " + e.getMessage());
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }
}
