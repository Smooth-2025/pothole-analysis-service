package com.smooth.pothole_analysis_service.pothole.service;

import com.smooth.pothole_analysis_service.global.exception.BusinessException;
import com.smooth.pothole_analysis_service.pothole.entity.PotholeData;
import com.smooth.pothole_analysis_service.pothole.exception.PotholeErrorCode;
import com.smooth.pothole_analysis_service.pothole.repository.PotholeDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

// RDS 데이터 저장 서비스 - Athena 쿼리 결과를 RDS에 저장하는 역할

@Service
@RequiredArgsConstructor
@Slf4j
public class PotholeService {

    private final PotholeDataRepository repository;

    // Athena 쿼리 결과를 RDS에 저장
    @Transactional
    public void saveQueryResults(String queryExecutionId, List<Map<String, String>> queryResults) {
        try {
            log.info("Athena 쿼리 결과 RDS 저장 시작 - {} 건", queryResults.size());

            int savedCount = 0;
            int duplicateCount = 0;
            int errorCount = 0;

            for (Map<String, String> row : queryResults) {
                try {
                    String carId = row.get("carid");
                    Double locationX = parseDouble(row.get("locationx"));
                    Double locationY = parseDouble(row.get("locationy"));
                    String detectedAt = formatDateOnly(row.get("timestamp"));
                    Double impactForce = parseDouble(row.get("impactforce"));

                    // 중복 데이터 체크
                    if (repository.existsByUniqueFields(carId, locationX, locationY, detectedAt, impactForce)) {
                        log.debug("중복 데이터 스킵 - carId: {}, location: ({}, {}), date: {}, impact: {}", 
                                carId, locationX, locationY, detectedAt, impactForce);
                        duplicateCount++;
                        continue;
                    }

                    PotholeData entity = PotholeData.builder()
                            .carId(carId)
                            .speed(parseDouble(row.get("speed")))
                            .locationX(locationX)
                            .locationY(locationY)
                            .s3Url(row.get("s3url"))
                            .impactForce(impactForce)
                            .zAxisVibration(parseDouble(row.get("zaxisvibration")))
                            .detectedAt(detectedAt)
                            .status("unconfirmed") // 기본 상태
                            .build();

                    repository.save(entity);
                    savedCount++;

                } catch (Exception e) {
                    log.warn("개별 데이터 저장 실패: {}", row, e);
                    errorCount++;
                }
            }

            long totalCount = repository.count();
            log.info("RDS 저장 완료 - 성공: {}건, 중복 스킵: {}건, 실패: {}건, 전체: {}건",
                    savedCount, duplicateCount, errorCount, totalCount);

            if (savedCount == 0 && duplicateCount == 0) {
                throw new BusinessException(PotholeErrorCode.RDS_DATA_SAVE_FAILED);
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("RDS 데이터 저장 중 오류 발생", e);
            throw new BusinessException(PotholeErrorCode.RDS_DATA_SAVE_FAILED);
        }
    }

    // RDS에 저장된 포트홀 데이터 총 개수 조회
    public long getTotalPotholeCount() {
        try {
            long count = repository.count();
            log.debug("RDS 포트홀 데이터 총 개수: {} 건", count);
            return count;
        } catch (Exception e) {
            log.error("RDS 데이터 개수 조회 중 오류 발생", e);
            throw new BusinessException(PotholeErrorCode.RDS_CONNECTION_FAILED);
        }
    }

    // 숫자 문자열을 Double로 변환 (속도, 위치, 충격량, z축 흔들림 데이터)
    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            log.warn("숫자 변환 실패: {}", value);
            return null;
        }
    }

    // 타임스탬프를 "2025-08-27 13:35:52" -> "2025-08-27" 형식으로 변환
    private String formatDateOnly(String timestamp) {
        if (timestamp == null || timestamp.trim().isEmpty()) {
            return null;
        }

        try {
            // 공백이나 'T'로 구분된 날짜 부분만 추출
            String dateOnly = timestamp.trim();
            if (dateOnly.contains(" ")) {
                dateOnly = dateOnly.split(" ")[0];
            } else if (dateOnly.contains("T")) {
                dateOnly = dateOnly.split("T")[0];
            }

            // YYYY-MM-DD 형식 검증
            if (dateOnly.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return dateOnly;
            }

            log.warn("예상치 못한 타임스탬프 형식: {}", timestamp);
            return timestamp;

        } catch (Exception e) {
            log.warn("타임스탬프 변환 실패: {}", timestamp, e);
            return timestamp;
        }
    }
}