package com.smooth.pothole_analysis_service.pothole.repository;

import com.smooth.pothole_analysis_service.pothole.entity.Pothole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PotholeRepository extends JpaRepository<Pothole, Long> {
    
    @Query("SELECT p FROM Pothole p WHERE " +
           "(:start IS NULL OR p.detectedAt >= :start) AND " +
           "(:end IS NULL OR p.detectedAt <= :end) AND " +
           "(:confirmed IS NULL OR " +
           "  (:confirmed = true AND p.status = 'confirmed') OR " +
           "  (:confirmed = false AND p.status != 'confirmed'))")
    Page<Pothole> findPotholesWithFilters(
            @Param("start") String start,
            @Param("end") String end,
            @Param("confirmed") Boolean confirmed,
            Pageable pageable
    );
}