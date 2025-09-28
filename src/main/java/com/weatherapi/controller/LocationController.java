package com.weatherapi.controller;

import com.weatherapi.model.ApiResponse;
import com.weatherapi.model.LocationData;
import com.weatherapi.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;
    private int requestCount = 0;
    private long lastResetTime = System.currentTimeMillis();

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    private boolean checkRateLimit() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime > 60000) {
            requestCount = 0;
            lastResetTime = currentTime;
        }

        if (requestCount >= 10) {
            return false;
        }

        requestCount++;
        return true;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<LocationData>> searchLocations(@RequestParam String q) {

        System.out.println("🔍 Location search for: " + q);

        if (!checkRateLimit()) {
            return ResponseEntity.status(429)
                    .body(ApiResponse.error("Rate limit exceeded. Maximum 10 requests per minute."));
        }

        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Query parameter 'q' is required"));
        }

        try {
            LocationData locations = locationService.searchLocations(q.trim());
            return ResponseEntity.ok(ApiResponse.success(locations));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error searching locations: " + e.getMessage()));
        }
    }
}