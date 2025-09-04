package com.smooth.pothole_analysis_service.pothole.controller;

import com.smooth.pothole_analysis_service.global.common.ApiResponse;
import com.smooth.pothole_analysis_service.global.exception.BusinessException;
import com.smooth.pothole_analysis_service.pothole.dto.DataProcessingRequestDto;
import com.smooth.pothole_analysis_service.pothole.dto.DataProcessingResponseDto;
import com.smooth.pothole_analysis_service.pothole.dto.PotholeQueryResponseDto;
import com.smooth.pothole_analysis_service.pothole.dto.PotholeConfirmRequestDto;
import com.smooth.pothole_analysis_service.pothole.exception.PotholeErrorCode;
import com.smooth.pothole_analysis_service.pothole.service.DataProcessingService;
import com.smooth.pothole_analysis_service.pothole.service.PotholeQueryService;
import com.smooth.pothole_analysis_service.pothole.service.PotholeService;
import com.smooth.pothole_analysis_service.pothole.service.ScheduledDataProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

// 포트홀 데이터 처리 컨트롤러 [S3 데이터 → Athena 쿼리 → RDS 저장 파이프라인]
@Slf4j
@RestController
@RequestMapping("/api/pothole")
@RequiredArgsConstructor
public class DataProcessingController {

    private final DataProcessingService dataProcessingService;
    private final ScheduledDataProcessingService scheduledDataProcessingService;
    private final PotholeQueryService potholeQueryService;
    private final PotholeService potholeService;

    // Athena 쿼리 실행 후 결과를 RDS에 저장
    @PostMapping("/athena/result-save")
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

    // 스케줄러를 수동으로 실행 (테스트용)
    @PostMapping("/athena/run-scheduler")
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

    // 포트홀 데이터 조회 API
    @GetMapping("/data")
    public ResponseEntity<ApiResponse<PotholeQueryResponseDto>> getPotholeData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
            @RequestParam(required = false) Boolean confirmed) {
        
        try {
            // 기본값 설정
            LocalDate defaultStart = start != null ? start : LocalDate.of(2025, 8, 1);
            LocalDate defaultEnd = end != null ? end : LocalDate.of(2099, 12, 31);
            
            log.info("포트홀 데이터 조회 요청: page={}, start={}, end={}, confirmed={}", 
                    page, defaultStart, defaultEnd, confirmed);
            
            PotholeQueryResponseDto data = potholeQueryService.getPotholeData(page, defaultStart, defaultEnd, confirmed);
            return ResponseEntity.ok(ApiResponse.success("포트홀 목록 조회 성공", data));
            
        } catch (Exception e) {
            log.error("포트홀 데이터 조회 중 오류 발생", e);
            throw new BusinessException(PotholeErrorCode.DATA_PROCESSING_FAILED);
        }
    }

    // 포트홀 확정 처리 API
    @PostMapping("/data/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPothole(@RequestBody PotholeConfirmRequestDto requestDto) {
        log.info("포트홀 확정 처리 요청: potholeId={}", requestDto.getPotholeId());
        
        potholeService.confirmPothole(requestDto.getPotholeId());
        
        return ResponseEntity.ok(ApiResponse.success("포트홀 확정 처리가 완료되었습니다."));
    }
}
