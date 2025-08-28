package com.smooth.pothole_analysis_service.pothole.exception;

import com.smooth.pothole_analysis_service.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PotholeErrorCode implements ErrorCode {

    // Athena 쿼리 관련
    ATHENA_QUERY_EXECUTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 5001, "Athena 쿼리 실행에 실패했습니다."),
    ATHENA_QUERY_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, 5002, "Athena 쿼리 실행 시간이 초과되었습니다."),
    ATHENA_QUERY_CANCELLED(HttpStatus.BAD_REQUEST, 5003, "Athena 쿼리가 취소되었습니다."),
    ATHENA_INVALID_QUERY(HttpStatus.BAD_REQUEST, 5004, "잘못된 Athena 쿼리입니다."),
    ATHENA_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, 5005, "Athena 쿼리 결과를 찾을 수 없습니다."),

    // RDS 데이터 저장 관련
    RDS_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, 5011, "RDS 연결에 실패했습니다."),
    RDS_DATA_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 5012, "RDS 데이터 저장에 실패했습니다."),
    RDS_TRANSACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 5013, "RDS 트랜잭션 처리에 실패했습니다."),

    // S3 데이터 접근 관련
    S3_ACCESS_DENIED(HttpStatus.FORBIDDEN, 5021, "S3 데이터 접근 권한이 없습니다."),
    S3_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, 5022, "S3에서 데이터를 찾을 수 없습니다."),

    // 데이터 처리 관련
    INVALID_WHERE_CLAUSE(HttpStatus.BAD_REQUEST, 5031, "잘못된 WHERE 조건입니다."),
    EMPTY_QUERY_RESULT(HttpStatus.NO_CONTENT, 5032, "쿼리 결과가 비어있습니다."),
    DATA_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 5033, "데이터 처리 중 오류가 발생했습니다."),

    // AWS 서비스 관련
    AWS_CREDENTIALS_INVALID(HttpStatus.UNAUTHORIZED, 5041, "AWS 인증 정보가 유효하지 않습니다."),
    AWS_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, 5042, "AWS 서비스를 사용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}