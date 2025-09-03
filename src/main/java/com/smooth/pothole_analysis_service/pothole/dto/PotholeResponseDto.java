package com.smooth.pothole_analysis_service.pothole.dto;

import com.smooth.pothole_analysis_service.pothole.entity.Pothole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PotholeResponseDto {
    
    private String potholeId;
    private UserInfo user;
    private LocationInfo location;
    private String detectedAt;
    private Double impact;
    private Double shake;
    private Double speed;
    private String imageUrl;
    private Boolean confirmed;
    
    @Getter
    @Builder
    public static class UserInfo {
        private String userId;
        private String userName;
    }
    
    @Getter
    @Builder
    public static class LocationInfo {
        private Double latitude;
        private Double longitude;
    }
    
    public static PotholeResponseDto from(Pothole pothole) {
        // DB의 자동증가 id를 potholeId로 사용 (p- 접두사 추가)
        String potholeId = pothole.getId() != null ? "p-" + pothole.getId() : null;
        
        // status가 "confirmed"인지 확인하여 Boolean으로 변환
        Boolean confirmed = "confirmed".equals(pothole.getStatus());
        
        // Carla 좌표를 실제 위경도로 변환
        Double convertedLatitude = null;
        Double convertedLongitude = null;
        
        if (pothole.getLocationX() != null && pothole.getLocationY() != null) {
            // 기준점 설정
            double originLongitude = 127.00374;
            double originLatitude = 37.55807;
            double metersPerLonDegree = 89000.0;
            double metersPerLatDegree = 111139.0;
            
            // Carla 좌표
            double carlaX = pothole.getLocationX();
            double carlaY = pothole.getLocationY();
            
            // 위경도 변환
            convertedLongitude = Math.round((originLongitude + (carlaX / metersPerLonDegree)) * 100000.0) / 100000.0;
            convertedLatitude = Math.round((originLatitude + (carlaY / metersPerLatDegree)) * 100000.0) / 100000.0;
        }
        
        return PotholeResponseDto.builder()
                .potholeId(potholeId)
                .user(UserInfo.builder()
                        .userId(pothole.getCarId()) // car_id를 user_id로 사용
                        .userName("홍길동") // 실제 사용자명은 별도 테이블에서 조회 필요
                        .build())
                .location(LocationInfo.builder()
                        .latitude(convertedLatitude)
                        .longitude(convertedLongitude)
                        .build())
                .detectedAt(pothole.getDetectedAt())
                .impact(pothole.getImpactForce())
                .shake(pothole.getZAxisVibration())
                .speed(pothole.getSpeed())
                .imageUrl(pothole.getS3Url())
                .confirmed(confirmed)
                .build();
    }
}