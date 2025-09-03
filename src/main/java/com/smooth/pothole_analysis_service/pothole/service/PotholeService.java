package com.smooth.pothole_analysis_service.pothole.service;

import com.smooth.pothole_analysis_service.pothole.dto.PotholeListResponseDto;
import com.smooth.pothole_analysis_service.pothole.dto.PotholeResponseDto;
import com.smooth.pothole_analysis_service.pothole.entity.Pothole;
import com.smooth.pothole_analysis_service.pothole.exception.PotholeErrorCode;
import com.smooth.pothole_analysis_service.pothole.repository.PotholeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PotholeService {
    
    private final PotholeRepository potholeRepository;
    
    public PotholeListResponseDto getPotholes(int page, LocalDate start, LocalDate end, Boolean confirmed) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 날짜를 문자열로 변환 (detected_at이 varchar이므로)
        String startStr = start != null ? start.toString() : null;
        String endStr = end != null ? end.toString() : null;
        
        Page<PotholeResponseDto> potholeResponses = potholeRepository
                .findPotholesWithFilters(startStr, endStr, confirmed, pageable)
                .map(PotholeResponseDto::from);
        
        return PotholeListResponseDto.from(potholeResponses);
    }
    
    @Transactional
    public void confirmPothole(String potholeId) {
        // potholeId에서 "p-" 접두사 제거하여 실제 DB id 추출
        Long actualId;
        try {
            if (potholeId.startsWith("p-")) {
                actualId = Long.parseLong(potholeId.substring(2));
            } else {
                actualId = Long.parseLong(potholeId);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(PotholeErrorCode.POTHOLE_NOT_FOUND.getMessage());
        }
        
        // 포트홀 존재 여부 확인
        Pothole pothole = potholeRepository.findById(actualId)
                .orElseThrow(() -> new RuntimeException(PotholeErrorCode.POTHOLE_NOT_FOUND.getMessage()));
        
        // 이미 확정된 포트홀인지 확인
        if ("confirmed".equals(pothole.getStatus())) {
            throw new RuntimeException(PotholeErrorCode.ALREADY_CONFIRMED_POTHOLE.getMessage());
        }
        
        // 상태를 confirmed로 변경하고 updated_at 갱신
        pothole.setStatus("confirmed");
        pothole.setUpdatedAt(LocalDateTime.now());
        
        potholeRepository.save(pothole);
    }
}