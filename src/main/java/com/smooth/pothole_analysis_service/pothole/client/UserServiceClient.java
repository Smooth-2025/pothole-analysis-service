package com.smooth.pothole_analysis_service.pothole.client;

import com.smooth.pothole_analysis_service.pothole.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{userId}")
    UserResponseDto getUserById(@PathVariable("userId") String userId);
}
