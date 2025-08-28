package com.smooth.pothole_analysis_service.pothole.repository;

import com.smooth.pothole_analysis_service.pothole.entity.PotholeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PotholeDataRepository extends JpaRepository<PotholeData, Long> {
}