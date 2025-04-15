package com.toyProject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TestSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    // 소켓 테스트
    @GetMapping("/test-notify")
    public String testNotify(@AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", "알림 테스트: 잘 연결됐어요!");

        messagingTemplate.convertAndSendToUser(username, "/queue/notify", payload);

        return "보냄: " + username;
    }
}

