package com.smooth.pothole_analysis_service.pothole.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PotholeQueryResponseDto {
    
    private List<PotholeContentDto> content;
    private int page;
    private int totalPages;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PotholeContentDto {
        private String potholeId;
        private UserDto user;
        private LocationDto location;
        private String detectedAt;
        private Double impact;
        private Double shake;
        private Double speed;
        private String imageUrl;
        private boolean confirmed;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private String userId;
        private String userName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDto {
        private Double latitude;
        private Double longitude;
    }
}
