package com.smooth.pothole_analysis_service.pothole.controller;

import com.smooth.pothole_analysis_service.global.common.ApiResponse;
import com.smooth.pothole_analysis_service.pothole.exception.PotholeErrorCode;
import com.smooth.pothole_analysis_service.pothole.service.DataProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 포트홀 데이터 처리 컨트롤러 [S3 데이터 → Athena 쿼리 → RDS 저장 파이프라인]
@Slf4j
@RestController
@RequestMapping("/api/pothole/athena")
@RequiredArgsConstructor
public class DataProcessingController {

    private final DataProcessingService dataProcessingService;

    /**
     * 핵심 기능: Athena 쿼리 실행 후 결과를 RDS에 저장
     * POST /api/pothole/athena/result-save
     */
    @PostMapping("/result-save")
    public ResponseEntity<ApiResponse<String>> queryAndSaveToRds(@RequestBody Map<String, String> request) {
        try {
            String whereClause = request.get("whereClause");
            if (whereClause == null || whereClause.trim().isEmpty()) {
                log.warn("Empty whereClause provided");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(PotholeErrorCode.INVALID_WHERE_CLAUSE));
            }

            log.info("S3 → Athena → RDS 파이프라인 시작: {}", whereClause);
            String result = dataProcessingService.queryAndSaveToRds(whereClause);

            return ResponseEntity.ok(ApiResponse.success(result, "데이터 처리가 완료되었습니다."));

        } catch (Exception e) {
            log.error("데이터 처리 파이프라인 실행 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(PotholeErrorCode.DATA_PROCESSING_FAILED));
        }
    }

    /**
     * RDS 연결 상태 및 데이터 현황 확인
     * GET /api/pothole/athena/rds-conn
     */
    @GetMapping("/rds-conn")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkRdsHealth() {
        try {
            log.info("RDS 연결 상태 확인");
            long totalCount = dataProcessingService.getPotholeDataCount();

            Map<String, Object> healthInfo = Map.of(
                    "database", "pothole_analysis",
                    "table", "pothole_data",
                    "status", "healthy",
                    "totalRecords", totalCount
            );

            return ResponseEntity.ok(ApiResponse.success("RDS 연결이 정상입니다.", healthInfo));

        } catch (Exception e) {
            log.error("RDS 헬스 체크 실패", e);
            return ResponseEntity.status(503)
                    .body(ApiResponse.error(PotholeErrorCode.RDS_CONNECTION_FAILED));
        }
    }
}
