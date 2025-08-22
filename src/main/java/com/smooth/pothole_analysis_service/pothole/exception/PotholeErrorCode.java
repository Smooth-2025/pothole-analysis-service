package com.smooth.pothole_analysis_service.pothole.exception;

import com.smooth.pothole_analysis_service.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PotholeErrorCode implements ErrorCode {

    // 기본 사용자 관리
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 5001, "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, 1002, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, 1003, "비밀번호가 일치하지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, 1004, "이메일 인증이 완료되지 않았습니다."),
    USER_CANT_FOUND(HttpStatus.FORBIDDEN, 1005, "사용자를 안 찾고 싶습니다."),

    // 계정 상태
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, 1011, "계정이 잠금 상태입니다."),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, 1012, "비활성화된 계정입니다."),
    ACCOUNT_EXPIRED(HttpStatus.FORBIDDEN, 1013, "만료된 계정입니다."),

    // 권한 관련
    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, 1021, "권한이 부족합니다."),
    ADMIN_ONLY_ACCESS(HttpStatus.FORBIDDEN, 1022, "관리자만 접근 가능합니다.");


    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}

