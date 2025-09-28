package com.weatherapi.controller;

import com.weatherapi.model.ApiResponse;
import com.weatherapi.model.WeatherData;
import com.weatherapi.model.ForecastData;
import com.weatherapi.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;
    private int requestCount = 0;
    private long lastResetTime = System.currentTimeMillis();

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
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

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<WeatherData>> getCurrentWeather(
            @RequestParam String location) {

        System.out.println("🌤️  Current weather request for: " + location);

        if (!checkRateLimit()) {
            return ResponseEntity.status(429)
                    .body(ApiResponse.error("Rate limit exceeded. Maximum 10 requests per minute."));
        }

        if (location == null || location.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Location parameter is required"));
        }

        try {
            WeatherData weatherData = weatherService.getCurrentWeather(location.trim());
            return ResponseEntity.ok(ApiResponse.success(weatherData));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error fetching weather data: " + e.getMessage()));
        }
    }

    @GetMapping("/forecast")
    public ResponseEntity<ApiResponse<ForecastData>> getForecast(
            @RequestParam String location,
            @RequestParam(defaultValue = "5") int days) {

        System.out.println("📅 Forecast request for: " + location + ", days: " + days);

        if (!checkRateLimit()) {
            return ResponseEntity.status(429)
                    .body(ApiResponse.error("Rate limit exceeded. Maximum 10 requests per minute."));
        }

        if (location == null || location.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Location parameter is required"));
        }

        if (days < 1 || days > 10) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Days must be between 1 and 10"));
        }

        try {
            ForecastData forecastData = weatherService.getForecast(location.trim(), days);
            return ResponseEntity.ok(ApiResponse.success(forecastData));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error fetching forecast: " + e.getMessage()));
        }
    }
}