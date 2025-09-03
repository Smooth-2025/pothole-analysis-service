package com.smooth.pothole_analysis_service.pothole.service;

import com.smooth.pothole_analysis_service.global.exception.BusinessException;
import com.smooth.pothole_analysis_service.pothole.exception.PotholeErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 포트홀 데이터 처리 서비스 -  S3 데이터 → Athena 쿼리 → RDS 저장 파이프라인

@Slf4j
@Service
@RequiredArgsConstructor
public class DataProcessingService {

    private final AthenaQueryService athenaQueryService;
    private final PotholeService potholeService;

    // S3 → Athena 쿼리 (조건문) → RDS 저장 파이프라인
    @Transactional
    public String queryAndSaveToRds(String whereClause) {
        try {
            log.info("S3 → Athena → RDS 파이프라인 시작 (조건: {})", whereClause);

            // 1. S3 데이터에 대한 Athena 쿼리 실행
            String queryExecutionId = athenaQueryService.executeSelectWithConditions(whereClause);
            log.info("Athena 쿼리 실행 완료 - ID: {}", queryExecutionId);

            // 2. Athena 쿼리 결과 조회
            var queryResults = athenaQueryService.getQueryResults(queryExecutionId);
            log.info("Athena에서 {} 건의 데이터 조회 완료", queryResults.size());

            if (queryResults.isEmpty()) {
                log.warn("해당 기간에 데이터가 없습니다.");
                String result = String.format(
                        "파이프라인 실행 완료 - 쿼리 ID: %s, 처리된 데이터: 0건 (해당 기간 데이터 없음)",
                        queryExecutionId
                );
                log.info("S3 → Athena → RDS 파이프라인 완료: {}", result);
                return result;
            }

            // 3. RDS에 데이터 저장
            potholeService.saveQueryResults(queryExecutionId, queryResults);
            log.info("RDS에 {} 건의 데이터 저장 완료", queryResults.size());

            // 4. 저장 결과 확인
            long totalCount = potholeService.getTotalPotholeCount();

            String result = String.format(
                    "파이프라인 실행 완료 - 쿼리 ID: %s, 처리된 데이터: %d건, 전체 데이터: %d건",
                    queryExecutionId, queryResults.size(), totalCount
            );

            log.info("S3 → Athena → RDS 파이프라인 완료: {}", result);
            return result;

        } catch (BusinessException e) {
            log.error("비즈니스 로직 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("S3 → Athena → RDS 파이프라인 실행 중 오류 발생", e);
            throw new BusinessException(PotholeErrorCode.DATA_PROCESSING_FAILED);
        }
    }
}