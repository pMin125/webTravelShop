package com.toyProject.exception;

public enum ErrorCode {
    ALREADY_WAITING_PAYMENT("이미 결제 대기 중입니다. 결제를 완료해주세요."),
    ALREADY_JOINED("이미 참여 완료하였습니다."),
    ALREADY_IN_WAITING_LIST("이미 대기열에 등록되어 있습니다."),
    PARTICIPATION_NOT_FOUND("참여 이력이 존재하지 않습니다."),
    PRODUCT_NOT_FOUND("상품이 존재하지 않습니다."),
    ORDER_NOT_FOUND("주문 정보가 존재하지 않습니다."),
    PAYMENT_NOT_FOUND("결제 정보가 존재하지 않습니다."),
    PARTICIPATION_NOT_ALLOWED("현재 참여할 수 없는 상태입니다."),
    ALREADY_ADD_PRODUCT("장바구니에 해당 상품이 존재합니다.");

    public final String message;

    ErrorCode(String message) {
        this.message = message;
    }

}


