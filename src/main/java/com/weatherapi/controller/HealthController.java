package com.weatherapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Map<String, Object> healthCheck() {
        return Map.of(
                "status", "UP",
                "timestamp", java.time.LocalDateTime.now().toString(),
                "service", "Weather Aggregation API",
                "version", "1.0.0",
                "features", Map.of(
                        "caching", "ENABLED",
                        "sources", "OpenWeather, WeatherAPI",
                        "rate_limiting", "SIMULATED"
                )
        );
    }
}