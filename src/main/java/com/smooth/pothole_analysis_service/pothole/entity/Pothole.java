package com.smooth.pothole_analysis_service.pothole.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pothole_data")
@Getter
@Setter
@NoArgsConstructor
public class Pothole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "car_id")
    private String carId;
    
    @Column(name = "speed")
    private Double speed;
    
    @Column(name = "location_x")
    private Double locationX;
    
    @Column(name = "location_y")
    private Double locationY;
    
    @Column(name = "s3_url")
    private String s3Url;
    
    @Column(name = "impact_force")
    private Double impactForce;
    
    @Column(name = "z_axis_vibration")
    private Double zAxisVibration;
    
    @Column(name = "detected_at")
    private String detectedAt;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}