package com.smooth.pothole_analysis_service.pothole.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CoordinateConversionService {
    
    // 기준점(Origin)의 실제 경도, 위도
    private static final double ORIGIN_LONGITUDE = 127.00374;
    private static final double ORIGIN_LATITUDE = 37.55807;
    
    private static final double METERS_PER_LON_DEGREE = 89000.0;
    private static final double METERS_PER_LAT_DEGREE = 111139.0;
    
    /**
     * Carla 좌표계의 X, Y를 실제 위도, 경도로 변환
     * @param carlaX Carla X 좌표
     * @param carlaY Carla Y 좌표
     * @return [경도, 위도] 배열
     */
    public double[] convertToLatLon(Double carlaX, Double carlaY) {
        if (carlaX == null || carlaY == null) {
            log.warn("좌표 변환 실패: carlaX={}, carlaY={}", carlaX, carlaY);
            return new double[]{0.0, 0.0};
        }
        
        double convertedLongitude = Math.round((ORIGIN_LONGITUDE + (carlaX / METERS_PER_LON_DEGREE)) * 100000.0) / 100000.0;
        double convertedLatitude = Math.round((ORIGIN_LATITUDE + (carlaY / METERS_PER_LAT_DEGREE)) * 100000.0) / 100000.0;
        
        log.debug("좌표 변환: carlaX={}, carlaY={} -> longitude={}, latitude={}", 
                 carlaX, carlaY, convertedLongitude, convertedLatitude);
        
        return new double[]{convertedLongitude, convertedLatitude};
    }
}
