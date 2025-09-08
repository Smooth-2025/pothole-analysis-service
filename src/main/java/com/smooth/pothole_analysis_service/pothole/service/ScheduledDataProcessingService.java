package com.smooth.pothole_analysis_service.pothole.service;

import com.smooth.pothole_analysis_service.global.exception.BusinessException;
import com.smooth.pothole_analysis_service.pothole.dto.DataProcessingResponseDto;
import com.smooth.pothole_analysis_service.pothole.exception.PotholeErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledDataProcessingService {

    private final DataProcessingService dataProcessingService;
    private volatile boolean isRunning = false;

    // 매일 02시에 전날 데이터를 처리하는 스케줄러
    @Scheduled(cron = "0 10 15 * * *", zone = "Asia/Seoul")
    public void processYesterdayData() {
        if (isRunning) {
            log.warn("스케줄러가 이미 실행 중입니다. 중복 실행을 방지합니다.");
            return;
        }

        try {
            isRunning = true;

            // 전날 날짜 계산
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDate today = LocalDate.now();

            String startTimestamp = yesterday.atStartOfDay()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String endTimestamp = today.atStartOfDay()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 전날 데이터를 조회하는 WHERE 절 생성 (varchar 타입으로 문자열 비교)
            String whereClause = String.format(
                    "timestamp >= '%s' AND timestamp < '%s'",
                    startTimestamp, endTimestamp);

            log.info("스케줄된 데이터 처리 시작 - WHERE: {}", whereClause);

            DataProcessingResponseDto result = dataProcessingService.queryAndSaveToRds(whereClause);

            log.info("스케줄된 데이터 처리 완료 - 처리된 데이터: {}건, 전체 데이터: {}건", result.getProcessedDataCount(), result.getTotalDataCount());

        } catch (BusinessException e) {
            log.error("스케줄된 데이터 처리 중 비즈니스 오류 발생: {}", e.getMessage());
            throw new BusinessException(PotholeErrorCode.SCHEDULER_EXECUTION_FAILED,
                    "스케줄된 데이터 처리 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("스케줄된 데이터 처리 중 예상치 못한 오류 발생", e);
            throw new BusinessException(PotholeErrorCode.SCHEDULER_EXECUTION_FAILED,
                    "스케줄된 데이터 처리 중 예상치 못한 오류가 발생했습니다.");
        } finally {
            isRunning = false;
        }
    }

    // 수동 실행
    public void executeManually() {
        if (isRunning) {
            throw new BusinessException(PotholeErrorCode.SCHEDULER_ALREADY_RUNNING);
        }
        processYesterdayData();
    }
}
