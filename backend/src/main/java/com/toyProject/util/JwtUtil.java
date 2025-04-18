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
    private final String secretKey = "LiLWe5swxwvcuUCUXIvOG6crppxJUgTyAczWRKSvkDo=";
    private long expirationTime = 3600000;

    public Authentication getAuthentication(String token) {

        try {
            String username = getUsernameFromToken(token);
            UserDetails userDetails = customUserService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(userDetails, null);
        } catch (Exception e) {
            log.error("getAuthentication 실패: {}", e.getMessage());
            return null;
        }
    }


    public String generateToken(String username) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Base64.getDecoder().decode(secretKey))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();

            return !expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            System.out.println("토큰이 만료되었습니다: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("토큰 검증 실패: " + e.getMessage());
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
