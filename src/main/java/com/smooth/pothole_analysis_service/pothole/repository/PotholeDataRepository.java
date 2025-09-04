package com.smooth.pothole_analysis_service.pothole.repository;

import com.smooth.pothole_analysis_service.pothole.entity.PotholeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PotholeDataRepository extends JpaRepository<PotholeData, Long> {
    
    // 중복 데이터 체크 - 동일한 차량, 위치, 날짜, 충격량을 가진 데이터가 있는지 확인
    @Query("SELECT COUNT(p) > 0 FROM PotholeData p WHERE " +
           "p.carId = :carId AND " +
           "p.locationX = :locationX AND " +
           "p.locationY = :locationY AND " +
           "p.detectedAt = :detectedAt AND " +
           "p.impactForce = :impactForce")
    boolean existsByUniqueFields(@Param("carId") String carId,
                                @Param("locationX") Double locationX,
                                @Param("locationY") Double locationY,
                                @Param("detectedAt") String detectedAt,
                                @Param("impactForce") Double impactForce);
}