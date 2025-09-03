package com.smooth.pothole_analysis_service.pothole.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PotholeListResponseDto {
    
    private List<PotholeResponseDto> content;
    private int page;
    private int totalPages;
    
    public static PotholeListResponseDto from(Page<PotholeResponseDto> page) {
        return PotholeListResponseDto.builder()
                .content(page.getContent())
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .build();
    }
}