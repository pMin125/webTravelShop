package com.toyProject.exception;

import lombok.Getter;

@Getter
public class ParticipationException extends RuntimeException {
    private final ErrorCode errorCode;

    public ParticipationException(ErrorCode errorCode) {
        super(errorCode.message);
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return "[%s] %s".formatted(errorCode.name(), errorCode.message);
    }
}

