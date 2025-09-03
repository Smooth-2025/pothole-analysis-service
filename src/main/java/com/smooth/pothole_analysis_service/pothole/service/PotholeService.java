package com.smooth.pothole_analysis_service.pothole.service;

import com.smooth.pothole_analysis_service.pothole.dto.PotholeListResponseDto;
import com.smooth.pothole_analysis_service.pothole.dto.PotholeResponseDto;
import com.smooth.pothole_analysis_service.pothole.repository.PotholeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
}