package com.smooth.pothole_analysis_service.pothole.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataProcessingRequestDto {
    @NotNull(message = "impactForceMin 값은 필수입니다")
    @JsonProperty("impactForceMin")
    private Double impactForceMin;

    @NotNull(message = "zAxisVibrationMin 값은 필수입니다")
    @JsonProperty("zAxisVibrationMin")
    private Double zAxisVibrationMin;
}
