package com.smooth.pothole_analysis_service.pothole.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PotholeConfirmRequestDto {
    private String potholeId;
}