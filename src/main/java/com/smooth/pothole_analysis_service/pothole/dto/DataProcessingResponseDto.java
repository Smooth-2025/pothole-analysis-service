package com.smooth.pothole_analysis_service.pothole.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataProcessingResponseDto {
    
    @JsonProperty("queryExecutionId")
    private String queryExecutionId;
    
    @JsonProperty("processedDataCount")
    private int processedDataCount;
    
    @JsonProperty("totalDataCount")
    private long totalDataCount;
    
    @JsonProperty("message")
    private String message;
    
    // 데이터가 없는 경우를 위한 생성자
    public static DataProcessingResponseDto noData(String queryExecutionId) {
        return new DataProcessingResponseDto(
            queryExecutionId, 
            0, 
            0L, 
            "해당 기간에 데이터가 없습니다."
        );
    }
    
    // 정상 처리된 경우를 위한 생성자
    public static DataProcessingResponseDto success(String queryExecutionId, int processedCount, long totalCount) {
        return new DataProcessingResponseDto(
            queryExecutionId,
            processedCount,
            totalCount,
            String.format("데이터 처리가 완료되었습니다. 처리된 데이터: %d건, 전체 데이터: %d건", processedCount, totalCount)
        );
    }
}