package com.smooth.pothole_analysis_service.pothole.exception;

import com.smooth.pothole_analysis_service.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PotholeErrorCode implements ErrorCode {

    // 포트홀 조회 관련
    POTHOLE_NOT_FOUND(HttpStatus.NOT_FOUND, 5001, "포트홀 데이터를 찾을 수 없습니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, 5002, "잘못된 날짜 범위입니다."),
    INVALID_PAGE_PARAMETER(HttpStatus.BAD_REQUEST, 5003, "잘못된 페이지 파라미터입니다.");
 
    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}

