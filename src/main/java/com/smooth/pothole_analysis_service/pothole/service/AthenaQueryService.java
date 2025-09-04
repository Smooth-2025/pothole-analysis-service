package com.smooth.pothole_analysis_service.pothole.service;

import com.smooth.pothole_analysis_service.global.exception.BusinessException;
import com.smooth.pothole_analysis_service.pothole.exception.PotholeErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

// Athena 쿼리 서비스 - S3 데이터에 대한 Athena 쿼리 실행 담당

@Slf4j
@Service
@RequiredArgsConstructor
public class AthenaQueryService {

    private final AthenaClient athenaClient;

    @Value("${aws.athena.database}")
    private String database;

    @Value("${aws.athena.output-location}")
    private String outputLocation;

    @Value("${aws.athena.workgroup}")
    private String workgroup;

    @Value("${aws.athena.tables.raw-pothole-data}")
    private String rawPotholeDataTable;

    // S3 데이터에 대한 Athena 쿼리 실행

    public String executeQuery(String query) {
        try {
            log.info("S3 데이터 Athena 쿼리 실행: {}", query);

            // 쿼리 실행 요청 생성
            QueryExecutionContext queryExecutionContext = QueryExecutionContext.builder()
                    .database(database)
                    .build();

            ResultConfiguration resultConfiguration = ResultConfiguration.builder()
                    .outputLocation(outputLocation)
                    .build();

            StartQueryExecutionRequest startQueryExecutionRequest = StartQueryExecutionRequest.builder()
                    .queryString(query)
                    .queryExecutionContext(queryExecutionContext)
                    .resultConfiguration(resultConfiguration)
                    .workGroup(workgroup)
                    .build();

            // Athena 쿼리 실행
            StartQueryExecutionResponse startQueryExecutionResponse =
                    athenaClient.startQueryExecution(startQueryExecutionRequest);

            String queryExecutionId = startQueryExecutionResponse.queryExecutionId();
            log.info("Athena 쿼리 실행 시작 - ID: {}", queryExecutionId);

            // 쿼리 완료까지 대기
            waitForQueryToComplete(queryExecutionId);

            log.info("Athena 쿼리 실행 완료 - ID: {}", queryExecutionId);
            return queryExecutionId;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Athena 쿼리 실행 중 오류 발생", e);
            throw new BusinessException(PotholeErrorCode.ATHENA_QUERY_EXECUTION_FAILED);
        }
    }

    // Athena 쿼리 완료까지 대기
    private void waitForQueryToComplete(String queryExecutionId) throws InterruptedException {
        GetQueryExecutionRequest getQueryExecutionRequest = GetQueryExecutionRequest.builder()
                .queryExecutionId(queryExecutionId)
                .build();

        GetQueryExecutionResponse getQueryExecutionResponse;
        boolean isQueryStillRunning = true;
        int waitCount = 0;
        final int maxWaitCount = 150; // 5분 타임아웃 (2초 * 150)

        while (isQueryStillRunning) {
            if (waitCount >= maxWaitCount) {
                log.error("Athena 쿼리 타임아웃 - ID: {}", queryExecutionId);
                throw new BusinessException(PotholeErrorCode.ATHENA_QUERY_TIMEOUT);
            }

            getQueryExecutionResponse = athenaClient.getQueryExecution(getQueryExecutionRequest);
            String queryState = getQueryExecutionResponse.queryExecution().status().state().toString();

            log.debug("Athena 쿼리 상태: {} ({}초 대기)", queryState, waitCount * 2);

            if (queryState.equals(QueryExecutionState.FAILED.toString())) {
                String reason = getQueryExecutionResponse.queryExecution().status().stateChangeReason();
                log.error("Athena 쿼리 실패 - ID: {}, 사유: {}", queryExecutionId, reason);
                throw new BusinessException(PotholeErrorCode.ATHENA_QUERY_EXECUTION_FAILED);
            } else if (queryState.equals(QueryExecutionState.CANCELLED.toString())) {
                log.error("Athena 쿼리 취소됨 - ID: {}", queryExecutionId);
                throw new BusinessException(PotholeErrorCode.ATHENA_QUERY_CANCELLED);
            } else if (queryState.equals(QueryExecutionState.SUCCEEDED.toString())) {
                isQueryStillRunning = false;
            } else {
                // 쿼리가 아직 실행 중이면 잠시 대기
                TimeUnit.SECONDS.sleep(2);
                waitCount++;
            }
        }
    }

    // S3 포트홀 데이터 조건부 조회
    public String executeSelectWithConditions(String whereClause) {
        if (whereClause == null || whereClause.trim().isEmpty()) {
            throw new BusinessException(PotholeErrorCode.INVALID_WHERE_CLAUSE);
        }

        String query = String.format("SELECT * FROM %s.%s WHERE %s", database, rawPotholeDataTable, whereClause);
        log.info("S3 조건부 데이터 조회 쿼리 실행: {}", whereClause);
        return executeQuery(query);
    }

    // Athena 쿼리 결과 조회
    public List<Map<String, String>> getQueryResults(String queryExecutionId) {
        List<Map<String, String>> results = new ArrayList<>();

        try {
            log.info("Athena 쿼리 결과 조회 - ID: {}", queryExecutionId);

            GetQueryResultsRequest getQueryResultsRequest = GetQueryResultsRequest.builder()
                    .queryExecutionId(queryExecutionId)
                    .build();

            GetQueryResultsResponse getQueryResultsResponse = athenaClient.getQueryResults(getQueryResultsRequest);

            List<Row> rows = getQueryResultsResponse.resultSet().rows();

            if (rows.isEmpty()) {
                log.warn("Athena 쿼리 결과가 비어있음 - ID: {}", queryExecutionId);
                return results;
            }

            // 첫 번째 행은 헤더
            Row headerRow = rows.get(0);
            List<String> columnNames = headerRow.data().stream()
                    .map(Datum::varCharValue)
                    .toList();

            // 데이터 행들 처리
            for (int i = 1; i < rows.size(); i++) {
                Row dataRow = rows.get(i);
                Map<String, String> rowData = new HashMap<>();

                List<Datum> data = dataRow.data();
                for (int j = 0; j < columnNames.size() && j < data.size(); j++) {
                    String columnName = columnNames.get(j);
                    String value = data.get(j).varCharValue();
                    rowData.put(columnName, value);
                }

                results.add(rowData);
            }

            log.info("Athena 쿼리 결과 조회 완료 - {} 건", results.size());
            return results;

        } catch (Exception e) {
            log.error("Athena 쿼리 결과 조회 중 오류 발생 - ID: {}", queryExecutionId, e);
            throw new BusinessException(PotholeErrorCode.ATHENA_RESULT_NOT_FOUND);
        }
    }


}