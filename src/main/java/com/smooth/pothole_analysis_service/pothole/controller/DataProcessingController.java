package com.smooth.pothole_analysis_service.pothole.controller;

import com.smooth.pothole_analysis_service.global.common.ApiResponse;
import com.smooth.pothole_analysis_service.global.exception.BusinessException;
import com.smooth.pothole_analysis_service.pothole.dto.DataProcessingRequestDto;
import com.smooth.pothole_analysis_service.pothole.dto.DataProcessingResponseDto;
import com.smooth.pothole_analysis_service.pothole.exception.PotholeErrorCode;
import com.smooth.pothole_analysis_service.pothole.service.DataProcessingService;
import com.smooth.pothole_analysis_service.pothole.service.ScheduledDataProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 포트홀 데이터 처리 컨트롤러 [S3 데이터 → Athena 쿼리 → RDS 저장 파이프라인]
@Slf4j
@RestController
@RequestMapping("/api/pothole/athena")
@RequiredArgsConstructor
public class DataProcessingController {

    private final DataProcessingService dataProcessingService;
    private final ScheduledDataProcessingService scheduledDataProcessingService;

    /**
     * 핵심 기능: Athena 쿼리 실행 후 결과를 RDS에 저장
     * POST /api/pothole/athena/result-save
     */
    @PostMapping("/result-save")
    public ResponseEntity<ApiResponse<DataProcessingResponseDto>> queryAndSaveToRds(
            @RequestBody DataProcessingRequestDto requestDto) {
        try {
            Double impact = requestDto.getImpactForceMin();
            Double zVib = requestDto.getZAxisVibrationMin();

            String whereClause = String.format("impactForce >= %s AND zAxisVibration >= %s", impact, zVib);

            log.info("S3 → Athena → RDS 파이프라인 시작: {}", whereClause);

            DataProcessingResponseDto result = dataProcessingService.queryAndSaveToRds(whereClause);
            return ResponseEntity.ok(ApiResponse.success("데이터 처리가 완료되었습니다.", result));

        } catch (Exception e) {
            log.error("데이터 처리 파이프라인 실행 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(PotholeErrorCode.DATA_PROCESSING_FAILED));
        }
    }

    /**
     * 스케줄러를 수동으로 실행 (테스트용)
     * POST /api/pothole/athena/run-scheduler
     */
    @PostMapping("/run-scheduler")
    public ResponseEntity<ApiResponse<Void>> runSchedulerManually() {
        try {
            log.info("스케줄러 수동 실행 요청");
            scheduledDataProcessingService.executeManually();
            
            return ResponseEntity.ok(ApiResponse.success("스케줄러가 수동으로 실행되었습니다."));

        } catch (Exception e) {
            log.error("스케줄러 수동 실행 중 오류 발생", e);
            throw new BusinessException(PotholeErrorCode.SCHEDULER_EXECUTION_FAILED);
        }
    }
}
