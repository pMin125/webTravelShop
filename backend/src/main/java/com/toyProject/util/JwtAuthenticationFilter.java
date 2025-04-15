package com.toyProject.util;

import com.toyProject.service.CustomUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtUtil jwtUtil;
    private final CustomUserService customUserService;

    // private final JwtBlacklistService jwtBlacklistService; // 🔴 일단 주석 처리해서 오류 방지

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("✅ Security 필터 실행됨! 요청 URI: " + request.getRequestURI());
        String token = resolveToken(request);
        System.out.println("tokentokentoken: " + token);

        // 🔴 블랙리스트 검증 부분을 임시로 주석 처리
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            System.out.println("추출된 사용자 이름: " + username);

            UserDetails userDetails = customUserService.loadUserByUsername(username);
            System.out.println("UserDetails 로드됨: " + userDetails.getUsername());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            // SLF4J 포맷팅이 아닌 문자열 결합으로 로그 출력
            System.out.println("Authentication token created with principal type: " + authentication.getPrincipal().getClass());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("SecurityContext에 인증 정보 설정됨: " + authentication.getPrincipal());
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + bearerToken);

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        if (bearerToken == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("cookie_token".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
        }
        return null;
    }
//    private String resolveToken(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7);
//        }
//
//        if (bearerToken == null) {
//            Cookie[] cookies = request.getCookies();
//            if (cookies != null) {
//                for (Cookie cookie : cookies) {
//                    if ("onion_token".equals(cookie.getName())) {
//                        return cookie.getValue();
//                    }
//                }
//            }
//        }
//
//        return null;
//    }
}


