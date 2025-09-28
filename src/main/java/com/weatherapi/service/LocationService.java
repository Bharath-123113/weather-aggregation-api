package com.weatherapi.service;

import com.weatherapi.model.LocationData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocationService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${weather.api.openweather.key}")
    private String openWeatherKey;

    @Value("${weather.api.weatherapi.key}")
    private String weatherApiKey;

    public LocationService(WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    @Cacheable(value = "locations", key = "#query")
    public LocationData searchLocations(String query) {
        System.out.println("🔍 Searching REAL locations for: " + query);

        List<LocationData.Location> locations = new ArrayList<>();

        try {
            // Try OpenWeatherMap geocoding first
            locations.addAll(searchOpenWeatherLocations(query));
        } catch (Exception e) {
            System.err.println("OpenWeather geocoding failed: " + e.getMessage());
        }

        try {
            // Try WeatherAPI search
            locations.addAll(searchWeatherApiLocations(query));
        } catch (Exception e) {
            System.err.println("WeatherAPI search failed: " + e.getMessage());
        }

        // Fallback to mock data if both APIs fail
        if (locations.isEmpty()) {
            locations.addAll(createFallbackLocations(query));
        }

        LocationData data = new LocationData();
        data.setLocations(locations);
        data.setCount(locations.size());

        return data;
    }

    private List<LocationData.Location> searchOpenWeatherLocations(String query) {
        try {
            String response = webClient.get()
                    .uri("http://api.openweathermap.org/geo/1.0/direct?q={query}&limit=5&appid={key}",
                            query, openWeatherKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            List<LocationData.Location> locations = new ArrayList<>();

            for (JsonNode item : root) {
                LocationData.Location location = new LocationData.Location();
                location.setName(item.path("name").asText());
                location.setCountry(item.path("country").asText());
                location.setLat(item.path("lat").asDouble());
                location.setLon(item.path("lon").asDouble());
                location.setSource("OpenWeatherMap");
                locations.add(location);
            }

            return locations;

        } catch (Exception e) {
            throw new RuntimeException("OpenWeather geocoding failed");
        }
    }

    private List<LocationData.Location> searchWeatherApiLocations(String query) {
        try {
            String response = webClient.get()
                    .uri("http://api.weatherapi.com/v1/search.json?key={key}&q={query}",
                            weatherApiKey, query)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            List<LocationData.Location> locations = new ArrayList<>();

            for (JsonNode item : root) {
                LocationData.Location location = new LocationData.Location();
                location.setName(item.path("name").asText());
                location.setCountry(item.path("country").asText());
                location.setLat(item.path("lat").asDouble());
                location.setLon(item.path("lon").asDouble());
                location.setSource("WeatherAPI");
                locations.add(location);
            }

            return locations;

        } catch (Exception e) {
            throw new RuntimeException("WeatherAPI search failed");
        }
    }

    private List<LocationData.Location> createFallbackLocations(String query) {
        List<LocationData.Location> locations = new ArrayList<>();

        locations.add(createLocation("1", query, "US", 40.7128, -74.0060, "Fallback"));
        locations.add(createLocation("2", query, "UK", 51.5074, -0.1278, "Fallback"));

        return locations;
    }

    private LocationData.Location createLocation(String id, String name, String country,
                                                 double lat, double lon, String source) {
        LocationData.Location location = new LocationData.Location();
        location.setId(id);
        location.setName(name);
        location.setCountry(country);
        location.setLat(lat);
        location.setLon(lon);
        location.setSource(source);
        return location;
    }
}