package com.weatherapi.model;

import java.util.List;

public class ForecastData {
    private Location location;
    private List<ForecastDay> forecast;
    private List<String> sources;

    public static class Location {
        private String name;
        private String country;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }

    public static class ForecastDay {
        private String date;
        private Double maxTemp;
        private Double minTemp;
        private String condition;
        private Integer humidity;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Double getMaxTemp() { return maxTemp; }
        public void setMaxTemp(Double maxTemp) { this.maxTemp = maxTemp; }
        public Double getMinTemp() { return minTemp; }
        public void setMinTemp(Double minTemp) { this.minTemp = minTemp; }
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public Integer getHumidity() { return humidity; }
        public void setHumidity(Integer humidity) { this.humidity = humidity; }
    }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public List<ForecastDay> getForecast() { return forecast; }
    public void setForecast(List<ForecastDay> forecast) { this.forecast = forecast; }
    public List<String> getSources() { return sources; }
    public void setSources(List<String> sources) { this.sources = sources; }
}