package com.toyProject.controller.handler;

import com.toyProject.exception.ParticipationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ParticipationException.class)
    public ResponseEntity<Map<String, String>> handleParticipation(ParticipationException ex) {
        Map<String, String> body = Map.of(
                "error", ex.getErrorCode().name(),
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}

