package com.weatherapi.model;

import java.util.List;

public class LocationData {
    private List<Location> locations;
    private Integer count;

    public static class Location {
        private String id;
        private String name;
        private String country;
        private Double lat;
        private Double lon;
        private String source;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLon() { return lon; }
        public void setLon(Double lon) { this.lon = lon; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }

    public List<Location> getLocations() { return locations; }
    public void setLocations(List<Location> locations) { this.locations = locations; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}