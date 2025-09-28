🌤️ Weather Aggregation API
A Spring Boot REST API that aggregates real-time weather data from multiple sources with intelligent caching and rate limiting.

📋 Project Overview
This API provides real-time weather data aggregation from multiple sources with built-in optimization features:

Multi-source data aggregation from OpenWeatherMap and WeatherAPI

Intelligent caching with 10-minute TTL to reduce API calls

Rate limiting to prevent abuse (10 requests/minute)

Comprehensive error handling and logging

🚀 Quick Start
Prerequisites
Java 17 or higher

Maven 3.6+

Internet connection

Postman (for API testing)

Installation & Setup
Clone and setup the project structure:

bash
# Create project directory
mkdir weather-aggregation-api
cd weather-aggregation-api

# Create folder structure
mkdir -p src/main/java/com/weatherapi/controller
mkdir -p src/main/java/com/weatherapi/service
mkdir -p src/main/java/com/weatherapi/model
mkdir -p src/main/java/com/weatherapi/config
mkdir -p src/main/resources
Add all the source files to their respective directories

🔐 API Configuration Setup
For Reviewers/Users:
The application.properties file is excluded from Git for security reasons. To run this application, please create your own configuration file:

Step 1: Create Configuration File
Create src/main/resources/application.properties with the following content:

server.port=8080
spring.cache.cache-names=weather,forecast,locations
logging.level.com.weatherapi=INFO

# Real API Configuration
weather.api.openweather.key=your_api_key
weather.api.openweather.url=https://api.openweathermap.org/data/2.5

weather.api.weatherapi.key=your_api_key
weather.api.weatherapi.url=http://api.weatherapi.com/v1

# Cache TTL in seconds (10 minutes)
cache.ttl.weather=600
cache.ttl.forecast=1800

OpenWeatherMap (Free: 1000 calls/day)

Sign up: https://openweathermap.org/api

Get API key from your account dashboard

WeatherAPI (Free: 1M calls/month)

Sign up: https://www.weatherapi.com/

Get API key from your account

Clone the repository

Create application.properties as shown above

Add your API keys (optional)

Run mvn spring-boot:run

Test endpoints with real weather data

GET
🔧 Troubleshooting
Application won't start:
Ensure application.properties exists in src/main/resources/

Build the project:

bash
mvn clean install
Run the application:

bash
mvn spring-boot:run
Verify it's working in Postman:

Open Postman

Create new GET request: http://localhost:8080/health

Send request → Should return HTTP 200 with service status

🎯 API Endpoints
Base URL: http://localhost:8080
Endpoint	Method	Parameters	Description
/health	GET	None	Service health check
/weather/current	GET	location (required)	Current weather data
/weather/forecast	GET	location (required), days (optional)	Weather forecast
/locations/search	GET	q (required)	Location search

📸 API Demo Screenshots
These scrren shots located in this project structure(weather-aggregation-api/screenshots/)
Health Check:
weather-aggregation-api/screenshots/HealthStatus.png
Health monitoring endpoint showing service status

Current Weather API
weather-aggregation-api/screenshots/Current Weather.png
Real-time weather data for London with aggregated sources

Weather Forecast
weather-aggregation-api/screenshots/Weather Forecast.png
*5-day weather forecast with temperature ranges*

Location Search
weather-aggregation-api/screenshots/Locaton Search.png
Intelligent location search with multiple results

Cache in Action
weather-aggregation-api/screenshots/Cache Testing.png
Console logs showing cache hits and misses

Rate Limiting
weather-aggregation-api/screenshots/Rate Limit.png
HTTP 429 response when rate limit is exceeded

🧪 Postman Testing Guide
1. Setting Up Postman
   Create a new Collection:

Open Postman

Click "Collections" → "New Collection"

Name: "Weather API Tests"

Add description: "Test suite for Weather Aggregation API"

2. Basic API Testing
   Request 1: Health Check

Method: GET

URL: http://localhost:8080/health

Expected Response: HTTP 200

json
{
"status": "UP",
"service": "Weather Aggregation API",
"version": "1.0.0"
}
Request 2: Current Weather

Method: GET

URL: http://localhost:8080/weather/current?location=London

Expected Response: HTTP 200 with weather data

Request 3: Weather Forecast

Method: GET

URL: http://localhost:8080/weather/forecast?location=London&days=3

Expected Response: HTTP 200 with forecast data

Request 4: Location Search

Method: GET

URL: http://localhost:8080/locations/search?q=London

Expected Response: HTTP 200 with location results

3. Testing Cache Functionality
   Step 1: First Request (Cache Miss)

Send GET request to: http://localhost:8080/weather/current?location=Tokyo

Check application console - should show "Fetching REAL weather data"

Note the response time (slower)

Step 2: Second Request (Cache Hit)

Immediately send same request again

Check application console - NO "Fetching" message

Note the response time (faster) - served from cache

4. Testing Rate Limiting
   Method 1: Manual Testing

Send 11+ GET requests quickly to: http://localhost:8080/weather/current?location=London

Expected Results:

Requests 1-10: HTTP 200 ✅

Request 11+: HTTP 429 ❌ (Rate limit exceeded)

Method 2: Using Postman Collection Runner

Add your request to the "Weather API Tests" collection

Click "Runner" button

Set:

Iterations: 12

Delay: 0 ms

Click "Run Weather API Tests"

Observe HTTP 429 responses after 10th request

Method 3: Using Postman Scripts
Add this to your request's Tests tab:

javascript
// Rate limiting test script
pm.test("Status code is 200", function () {
pm.response.to.have.status(200);
});

// Log request count
if (!pm.collectionVariables.get("requestCount")) {
pm.collectionVariables.set("requestCount", 1);
} else {
pm.collectionVariables.set("requestCount", pm.collectionVariables.get("requestCount") + 1);
}

console.log("Request #" + pm.collectionVariables.get("requestCount"));
5. Error Handling Tests
   Test 1: Invalid Parameters

URL: http://localhost:8080/weather/current?location=

Expected: HTTP 400 Bad Request

Test 2: Invalid Days Parameter

URL: http://localhost:8080/weather/forecast?location=London&days=15

Expected: HTTP 400 Bad Request

Test 3: Rate Limit Exceeded

URL: http://localhost:8080/weather/current?location=London (11th request)

Expected: HTTP 429 Too Many Requests

📊 Features Demonstration
1. Caching in Action
   text
   Postman + Console Logs Show:
- First request: "Fetching REAL weather data for: London"
- Second request (within 10 min): No message (served from cache)
- After 10 minutes: "Fetching REAL weather data" again
2. Rate Limiting
   Limit: 10 requests per minute per endpoint

Response when exceeded: HTTP 429 with error message

Protection: Prevents API abuse and ensures fair usage

3. Error Handling
   Invalid parameters → 400 Bad Request

Rate limit exceeded → 429 Too Many Requests

Service errors → 500 Internal Server Error

All errors include structured JSON responses

4. Multi-Source Aggregation
   Data combined from multiple weather providers

Fallback mechanisms if one service fails

Consistent response format across all sources

🔧 Configuration
Application Properties
Edit src/main/resources/application.properties:

properties
server.port=8080
spring.cache.cache-names=weather,forecast,locations
logging.level.com.weatherapi=INFO

# Optional: For real API integration
weather.api.openweather.key=your_api_key
weather.api.weatherapi.key=your_api_key
Cache Configuration
Weather Data: 10-minute TTL

Forecast Data: 30-minute TTL

Location Data: 60-minute TTL

Maximum Size: 1000 entries per cache

🏗️ Project Structure
text
weather-aggregation-api/
├── src/main/java/com/weatherapi/
│   ├── WeatherApiApplication.java
│   ├── controller/
│   │   ├── WeatherController.java
│   │   ├── LocationController.java
│   │   └── HealthController.java
│   ├── service/
│   │   ├── WeatherService.java
│   │   └── LocationService.java
│   ├── model/
│   │   ├── ApiResponse.java
│   │   ├── WeatherData.java
│   │   ├── ForecastData.java
│   │   └── LocationData.java
│   └── config/
│       ├── CacheConfig.java
│       └── WebClientConfig.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
💡 Implementation Approach
Core Design Principles
Separation of Concerns: Clear separation between controllers, services, and data models

Caching First: Aggressive caching to minimize external API calls

Graceful Degradation: Fallback mechanisms when external services fail

Rate Limiting: Protection against API abuse while maintaining performance

Key Technical Decisions
Spring Boot 3.2.0: For rapid development and production-ready features

Caffeine Cache: High-performance caching library

WebClient: Non-blocking HTTP client for external API calls

Structured Logging: Clear visibility into API operations and caching behavior

Performance Optimizations
Response Caching: 60-90% reduction in external API calls

Async Processing: Non-blocking I/O operations

Connection Pooling: Efficient HTTP client management

Input Validation: Early rejection of invalid requests

🐛 Troubleshooting
Common Issues
Application won't start:

Check Java version (requires Java 17+)

Verify all source files are in correct locations

Run mvn clean install to refresh dependencies

Dependencies not found:

bash
mvn clean compile -U
Port already in use:

properties
# Change in application.properties
server.port=8081
Cache not working in Postman:

Check console logs for "Fetching REAL weather data" messages

First request should show the message, second should not

Verify cache configuration in CacheConfig.java

Postman Testing Tips
Rate Limiting Not Working:

Ensure you're sending requests quickly (within 1 minute)

Use Postman Collection Runner for accurate timing

Check different endpoints have separate limits

Cache Not Evident:

Use different cities to see cache behavior

Monitor application console logs

Compare response times between first and subsequent requests

📈 Sample Responses
Successful Response (HTTP 200)
json
{
"status": "success",
"message": "Operation completed successfully",
"data": {
"location": {
"name": "London",
"country": "GB",
"lat": 51.5074,
"lon": -0.1278
},
"current": {
"temperature": 15.5,
"feelsLike": 13.2,
"humidity": 75,
"pressure": 1013,
"windSpeed": 5.2,
"condition": "Cloudy"
},
"sources": ["OpenWeatherMap", "WeatherAPI"]
},
"timestamp": "2024-01-15T10:30:45.123"
}
Rate Limit Exceeded (HTTP 429)
json
{
"status": "error",
"message": "Rate limit exceeded. Maximum 10 requests per minute.",
"data": null,
"timestamp": "2024-01-15T10:30:45.123"
}
Invalid Parameter (HTTP 400)
json
{
"status": "error",
"message": "Location parameter is required",
"data": null,
"timestamp": "2024-01-15T10:30:45.123"
}
🎉 Conclusion
This Weather Aggregation API demonstrates modern Spring Boot development practices with:

✅ RESTful API design

✅ Intelligent caching strategies

✅ Rate limiting and protection

✅ Comprehensive error handling

✅ Multi-source data aggregation

✅ Production-ready configuration

✅ Postman-friendly testing


