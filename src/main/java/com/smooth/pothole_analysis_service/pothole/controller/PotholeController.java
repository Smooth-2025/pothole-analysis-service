package com.smooth.pothole_analysis_service.pothole.controller;

import com.smooth.pothole_analysis_service.global.common.ApiResponse;
import com.smooth.pothole_analysis_service.pothole.dto.PotholeConfirmRequestDto;
import com.smooth.pothole_analysis_service.pothole.dto.PotholeListResponseDto;
import com.smooth.pothole_analysis_service.pothole.exception.PotholeErrorCode;
import com.smooth.pothole_analysis_service.pothole.service.PotholeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/pothole/data")
@RequiredArgsConstructor
public class PotholeController {
    
    private final PotholeService potholeService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<PotholeListResponseDto>> getPotholes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
            @RequestParam(required = false) Boolean confirmed
    ) {
        // 페이지 파라미터 검증
        if (page < 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(PotholeErrorCode.INVALID_PAGE_PARAMETER));
        }
        
        // 날짜 범위 검증
        if (start != null && end != null && start.isAfter(end)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(PotholeErrorCode.INVALID_DATE_RANGE));
        }
        
        PotholeListResponseDto response = potholeService.getPotholes(page, start, end, confirmed);
        
        return ResponseEntity.ok(ApiResponse.success("포트홀 목록 조회 성공", response));
    }
    
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPothole(@RequestBody PotholeConfirmRequestDto request) {
        try {
            potholeService.confirmPothole(request.getPotholeId());
            return ResponseEntity.ok(ApiResponse.success("포트홀 확정 처리가 완료되었습니다."));
        } catch (RuntimeException e) {
            // 포트홀을 찾을 수 없는 경우
            if (e.getMessage().equals(PotholeErrorCode.POTHOLE_NOT_FOUND.getMessage())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(PotholeErrorCode.POTHOLE_NOT_FOUND));
            }
            // 이미 확정된 포트홀인 경우
            if (e.getMessage().equals(PotholeErrorCode.ALREADY_CONFIRMED_POTHOLE.getMessage())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(PotholeErrorCode.ALREADY_CONFIRMED_POTHOLE));
            }
            // 기타 예외
            throw e;
        }
    }
}