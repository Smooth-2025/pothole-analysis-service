package com.smooth.pothole_analysis_service.pothole.repository;

import com.smooth.pothole_analysis_service.pothole.entity.PotholeData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    // 날짜 범위로 포트홀 데이터 조회
    @Query("SELECT p FROM PotholeData p WHERE p.detectedAt >= :startDate AND p.detectedAt <= :endDate ORDER BY p.detectedAt DESC")
    Page<PotholeData> findByDetectedAtBetween(@Param("startDate") String startDate, 
                                            @Param("endDate") String endDate, 
                                            Pageable pageable);
    
    // 날짜 범위와 상태로 포트홀 데이터 조회
    @Query("SELECT p FROM PotholeData p WHERE p.detectedAt >= :startDate AND p.detectedAt <= :endDate AND p.status = :status ORDER BY p.detectedAt DESC")
    Page<PotholeData> findByDetectedAtBetweenAndStatus(@Param("startDate") String startDate, 
                                                     @Param("endDate") String endDate, 
                                                     @Param("status") String status, 
                                                     Pageable pageable);
}
