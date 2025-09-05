package com.smooth.pothole_analysis_service.pothole.service;

import com.smooth.pothole_analysis_service.pothole.client.UserServiceClient;
import com.smooth.pothole_analysis_service.pothole.dto.PotholeQueryResponseDto;
import com.smooth.pothole_analysis_service.pothole.dto.UserResponseDto;
import com.smooth.pothole_analysis_service.pothole.entity.PotholeData;
import com.smooth.pothole_analysis_service.pothole.repository.PotholeDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PotholeQueryService {
    
    private final PotholeDataRepository potholeDataRepository;
    private final UserServiceClient userServiceClient;
    
    public PotholeQueryResponseDto getPotholeData(int page, LocalDate start, LocalDate end, Boolean confirmed) {
        Pageable pageable = PageRequest.of(page, 10); // 페이지당 10개
        
        String startStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        Page<PotholeData> potholeDataPage;
        
        if (confirmed != null) {
            // confirmed 상태에 따른 필터링 (예: "confirmed" 또는 "unconfirmed" 상태)
            String status = confirmed ? "confirmed" : "unconfirmed";
            potholeDataPage = potholeDataRepository.findByDetectedAtBetweenAndStatus(
                startStr, endStr, status, pageable);
        } else {
            potholeDataPage = potholeDataRepository.findByDetectedAtBetween(
                startStr, endStr, pageable);
        }
        
        List<PotholeQueryResponseDto.PotholeContentDto> content = potholeDataPage.getContent()
            .stream()
            .map(this::convertToPotholeContentDto)
            .collect(Collectors.toList());
        
        return PotholeQueryResponseDto.builder()
            .content(content)
            .page(page)
            .totalPages(potholeDataPage.getTotalPages())
            .build();
    }
    
    private PotholeQueryResponseDto.PotholeContentDto convertToPotholeContentDto(PotholeData potholeData) {
        // 사용자 정보 조회
        PotholeQueryResponseDto.UserDto userDto = getUserInfo(potholeData.getCarId());
        
        Double latitude = potholeData.getLatitude();
        Double longitude = potholeData.getLongitude();

        // 위치 정보 생성
        PotholeQueryResponseDto.LocationDto locationDto = PotholeQueryResponseDto.LocationDto.builder()
            .latitude(latitude)
            .longitude(longitude)
            .build();
        
        return PotholeQueryResponseDto.PotholeContentDto.builder()
            .potholeId("p-" + potholeData.getId())
            .user(userDto)
            .location(locationDto)
            .detectedAt(potholeData.getDetectedAt())
            .impact(potholeData.getImpactForce())
            .shake(potholeData.getZAxisVibration())
            .speed(potholeData.getSpeed())
            .imageUrl(potholeData.getS3Url())
            .confirmed("confirmed".equals(potholeData.getStatus()))
            .build();
    }
    
    private PotholeQueryResponseDto.UserDto getUserInfo(String carId) {
        try {
            UserResponseDto userResponse = userServiceClient.getUserById(Long.valueOf(carId));
            return PotholeQueryResponseDto.UserDto.builder()
                .userId(userResponse.getUserId())
                .userName(userResponse.getUserName())
                .build();
        } catch (Exception e) {
            log.warn("사용자 정보 조회 실패: carId={}", carId, e);
            return PotholeQueryResponseDto.UserDto.builder()
                .userId(carId)
                .userName("알 수 없음")
                .build();
        }
    }
}
