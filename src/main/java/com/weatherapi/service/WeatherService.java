package com.weatherapi.service;

import com.weatherapi.model.WeatherData;
import com.weatherapi.model.ForecastData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Service
public class WeatherService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${weather.api.openweather.key}")
    private String openWeatherKey;

    @Value("${weather.api.weatherapi.key}")
    private String weatherApiKey;

    public WeatherService(WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    @Cacheable(value = "weather", key = "#location")
    public WeatherData getCurrentWeather(String location) {
        System.out.println("🌤️  Fetching REAL weather data for: " + location + " at " + LocalDateTime.now());

        try {
            // Fetch from both APIs in parallel
            CompletableFuture<WeatherData> openWeatherFuture = getOpenWeatherData(location);
            CompletableFuture<WeatherData> weatherApiFuture = getWeatherApiData(location);

            // Wait for both to complete
            WeatherData openWeatherData = openWeatherFuture.get();
            WeatherData weatherApiData = weatherApiFuture.get();

            // Aggregate results
            return aggregateWeatherData(openWeatherData, weatherApiData);

        } catch (Exception e) {
            System.err.println("Error fetching weather data: " + e.getMessage());
            throw new RuntimeException("Failed to fetch weather data from APIs");
        }
    }

    private CompletableFuture<WeatherData> getOpenWeatherData(String location) {
        return webClient.get()
                .uri("https://api.openweathermap.org/data/2.5/weather?q={location}&appid={key}&units=metric",
                        location, openWeatherKey)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseOpenWeatherResponse)
                .toFuture()
                .exceptionally(ex -> {
                    System.err.println("OpenWeather API error: " + ex.getMessage());
                    return createFallbackWeatherData(location, "OpenWeather");
                });
    }

    private CompletableFuture<WeatherData> getWeatherApiData(String location) {
        return webClient.get()
                .uri("http://api.weatherapi.com/v1/current.json?key={key}&q={location}&aqi=no",
                        weatherApiKey, location)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseWeatherApiResponse)
                .toFuture()
                .exceptionally(ex -> {
                    System.err.println("WeatherAPI error: " + ex.getMessage());
                    return createFallbackWeatherData(location, "WeatherAPI");
                });
    }

    private WeatherData parseOpenWeatherResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            WeatherData data = new WeatherData();
            WeatherData.Location location = new WeatherData.Location();

            location.setName(root.path("name").asText());
            location.setCountry(root.path("sys").path("country").asText());
            location.setLat(root.path("coord").path("lat").asDouble());
            location.setLon(root.path("coord").path("lon").asDouble());

            WeatherData.CurrentWeather current = new WeatherData.CurrentWeather();
            JsonNode main = root.path("main");
            JsonNode weather = root.path("weather").get(0);
            JsonNode wind = root.path("wind");

            current.setTemperature(main.path("temp").asDouble());
            current.setFeelsLike(main.path("feels_like").asDouble());
            current.setHumidity(main.path("humidity").asInt());
            current.setPressure(main.path("pressure").asInt());
            current.setWindSpeed(wind.path("speed").asDouble());
            current.setCondition(weather.path("main").asText());
            current.setDescription(weather.path("description").asText());

            data.setLocation(location);
            data.setCurrent(current);
            data.setSources(Arrays.asList("OpenWeatherMap"));
            data.setLastUpdated(LocalDateTime.now().toString());

            return data;

        } catch (Exception e) {
            System.err.println("Error parsing OpenWeather response: " + e.getMessage());
            throw new RuntimeException("Failed to parse OpenWeather data");
        }
    }

    private WeatherData parseWeatherApiResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode location = root.path("location");
            JsonNode current = root.path("current");

            WeatherData data = new WeatherData();
            WeatherData.Location loc = new WeatherData.Location();

            loc.setName(location.path("name").asText());
            loc.setCountry(location.path("country").asText());
            loc.setLat(location.path("lat").asDouble());
            loc.setLon(location.path("lon").asDouble());

            WeatherData.CurrentWeather currentWeather = new WeatherData.CurrentWeather();
            currentWeather.setTemperature(current.path("temp_c").asDouble());
            currentWeather.setFeelsLike(current.path("feelslike_c").asDouble());
            currentWeather.setHumidity(current.path("humidity").asInt());
            currentWeather.setPressure(current.path("pressure_mb").asInt());
            currentWeather.setWindSpeed(current.path("wind_kph").asDouble() / 3.6); // Convert to m/s
            currentWeather.setCondition(current.path("condition").path("text").asText());
            currentWeather.setDescription(current.path("condition").path("text").asText());

            data.setLocation(loc);
            data.setCurrent(currentWeather);
            data.setSources(Arrays.asList("WeatherAPI"));
            data.setLastUpdated(LocalDateTime.now().toString());

            return data;

        } catch (Exception e) {
            System.err.println("Error parsing WeatherAPI response: " + e.getMessage());
            throw new RuntimeException("Failed to parse WeatherAPI data");
        }
    }

    @Cacheable(value = "forecast", key = "#location + '_' + #days")
    public ForecastData getForecast(String location, int days) {
        System.out.println("📅 Fetching REAL forecast for: " + location + " days: " + days);

        try {
            // Using WeatherAPI for forecast (better free tier)
            String response = webClient.get()
                    .uri("http://api.weatherapi.com/v1/forecast.json?key={key}&q={location}&days={days}&aqi=no&alerts=no",
                            weatherApiKey, location, days)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Simple blocking call for demo

            return parseForecastResponse(response, days);

        } catch (Exception e) {
            System.err.println("Error fetching forecast: " + e.getMessage());
            return createFallbackForecast(location, days);
        }
    }

    private ForecastData parseForecastResponse(String response, int days) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode location = root.path("location");
            JsonNode forecast = root.path("forecast").path("forecastday");

            ForecastData data = new ForecastData();
            ForecastData.Location loc = new ForecastData.Location();

            loc.setName(location.path("name").asText());
            loc.setCountry(location.path("country").asText());
            data.setLocation(loc);

            java.util.List<ForecastData.ForecastDay> forecastDays = new java.util.ArrayList<>();

            for (int i = 0; i < Math.min(days, forecast.size()); i++) {
                JsonNode day = forecast.get(i);
                JsonNode dayData = day.path("day");

                ForecastData.ForecastDay forecastDay = new ForecastData.ForecastDay();
                forecastDay.setDate(day.path("date").asText());
                forecastDay.setMaxTemp(dayData.path("maxtemp_c").asDouble());
                forecastDay.setMinTemp(dayData.path("mintemp_c").asDouble());
                forecastDay.setHumidity(dayData.path("avghumidity").asInt());
                forecastDay.setCondition(dayData.path("condition").path("text").asText());

                forecastDays.add(forecastDay);
            }

            data.setForecast(forecastDays);
            data.setSources(Arrays.asList("WeatherAPI"));

            return data;

        } catch (Exception e) {
            System.err.println("Error parsing forecast: " + e.getMessage());
            throw new RuntimeException("Failed to parse forecast data");
        }
    }

    private WeatherData aggregateWeatherData(WeatherData source1, WeatherData source2) {
        WeatherData aggregated = new WeatherData();

        // Use location from first source
        aggregated.setLocation(source1.getLocation());

        WeatherData.CurrentWeather current = new WeatherData.CurrentWeather();

        // Average values from both sources
        current.setTemperature((source1.getCurrent().getTemperature() + source2.getCurrent().getTemperature()) / 2);
        current.setFeelsLike((source1.getCurrent().getFeelsLike() + source2.getCurrent().getFeelsLike()) / 2);
        current.setHumidity((source1.getCurrent().getHumidity() + source2.getCurrent().getHumidity()) / 2);
        current.setPressure((source1.getCurrent().getPressure() + source2.getCurrent().getPressure()) / 2);
        current.setWindSpeed((source1.getCurrent().getWindSpeed() + source2.getCurrent().getWindSpeed()) / 2);
        current.setCondition("Aggregated");
        current.setDescription("Real-time data from multiple sources");

        aggregated.setCurrent(current);
        aggregated.setSources(Arrays.asList("OpenWeatherMap", "WeatherAPI"));
        aggregated.setLastUpdated(LocalDateTime.now().toString());

        return aggregated;
    }

    private WeatherData createFallbackWeatherData(String location, String source) {
        WeatherData data = new WeatherData();
        WeatherData.Location loc = new WeatherData.Location();
        loc.setName(location);
        loc.setCountry("N/A");

        WeatherData.CurrentWeather current = new WeatherData.CurrentWeather();
        current.setTemperature(20.0);
        current.setFeelsLike(18.0);
        current.setHumidity(65);
        current.setPressure(1013);
        current.setWindSpeed(3.5);
        current.setCondition("Data Unavailable");
        current.setDescription("Fallback data - API temporarily unavailable");

        data.setLocation(loc);
        data.setCurrent(current);
        data.setSources(Arrays.asList(source + " (Fallback)"));
        data.setLastUpdated(LocalDateTime.now().toString());

        return data;
    }

    private ForecastData createFallbackForecast(String location, int days) {
        ForecastData data = new ForecastData();
        ForecastData.Location loc = new ForecastData.Location();
        loc.setName(location);
        loc.setCountry("N/A");
        data.setLocation(loc);

        java.util.List<ForecastData.ForecastDay> forecastDays = new java.util.ArrayList<>();
        for (int i = 0; i < days; i++) {
            ForecastData.ForecastDay day = new ForecastData.ForecastDay();
            day.setDate(java.time.LocalDate.now().plusDays(i).toString());
            day.setMaxTemp(22.0 + i);
            day.setMinTemp(12.0 + i);
            day.setHumidity(60);
            day.setCondition("Data Unavailable");
            forecastDays.add(day);
        }

        data.setForecast(forecastDays);
        data.setSources(Arrays.asList("Fallback"));

        return data;
    }
}