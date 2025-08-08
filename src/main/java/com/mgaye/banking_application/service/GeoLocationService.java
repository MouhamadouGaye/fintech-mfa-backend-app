package com.mgaye.banking_application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mgaye.banking_application.dto.Location;

import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class GeoLocationService {

    private final RestTemplate restTemplate;
    private final Map<String, Location> locationCache = new ConcurrentHashMap<>();

    @Value("${app.geolocation.api.key:}")
    private String apiKey;

    @Value("${app.geolocation.enabled:false}")
    private boolean geoLocationEnabled;

    public GeoLocationService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Gets location information for an IP address
     */
    public Location getLocation(String ipAddress) {
        if (!geoLocationEnabled || ipAddress == null || isPrivateIp(ipAddress)) {
            return createDefaultLocation();
        }

        // Check cache first
        if (locationCache.containsKey(ipAddress)) {
            return locationCache.get(ipAddress);
        }

        try {
            Location location = fetchLocationFromApi(ipAddress);
            locationCache.put(ipAddress, location);
            return location;
        } catch (Exception e) {
            log.error("Failed to get location for IP: {}", ipAddress, e);
            return createDefaultLocation();
        }
    }

    /**
     * Calculates distance between two locations using Haversine formula
     */
    public double calculateDistance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return 0.0;
        }

        double lat1Rad = Math.toRadians(loc1.getLatitude());
        double lat2Rad = Math.toRadians(loc2.getLatitude());
        double deltaLatRad = Math.toRadians(loc2.getLatitude() - loc1.getLatitude());
        double deltaLonRad = Math.toRadians(loc2.getLongitude() - loc1.getLongitude());

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Earth's radius in kilometers
        final double EARTH_RADIUS_KM = 6371.0;
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Checks if two locations are in the same country
     */
    public boolean isSameCountry(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return false;
        }
        return loc1.getCountryCode() != null &&
                loc1.getCountryCode().equalsIgnoreCase(loc2.getCountryCode());
    }

    /**
     * Checks if two locations are in the same city
     */
    public boolean isSameCity(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return false;
        }
        return isSameCountry(loc1, loc2) &&
                loc1.getCity() != null &&
                loc1.getCity().equalsIgnoreCase(loc2.getCity());
    }

    /**
     * Gets country name from IP address
     */
    public String getCountryName(String ipAddress) {
        Location location = getLocation(ipAddress);
        return location != null ? location.getCountry() : "Unknown";
    }

    /**
     * Gets city name from IP address
     */
    public String getCityName(String ipAddress) {
        Location location = getLocation(ipAddress);
        return location != null ? location.getCity() : "Unknown";
    }

    private Location fetchLocationFromApi(String ipAddress) {
        try {
            // Using ip-api.com (free service) - replace with your preferred service
            String url = String.format(
                    "http://ip-api.com/json/%s?fields=status,country,countryCode,region,regionName,city,lat,lon,timezone,isp",
                    ipAddress);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                return Location.builder()
                        .country((String) response.get("country"))
                        .countryCode((String) response.get("countryCode"))
                        .region((String) response.get("regionName"))
                        .city((String) response.get("city"))
                        .latitude(((Number) response.get("lat")).doubleValue())
                        .longitude(((Number) response.get("lon")).doubleValue())
                        .timezone((String) response.get("timezone"))
                        .isp((String) response.get("isp"))
                        .ipAddress(ipAddress)
                        .build();
            }
        } catch (RestClientException e) {
            log.error("Error calling geolocation API for IP: {}", ipAddress, e);
        }

        return createDefaultLocation();
    }

    private Location createDefaultLocation() {
        return Location.builder()
                .country("Unknown")
                .countryCode("XX")
                .region("Unknown")
                .city("Unknown")
                .latitude(0.0)
                .longitude(0.0)
                .timezone("UTC")
                .isp("Unknown")
                .build();
    }

    private boolean isPrivateIp(String ipAddress) {
        if (ipAddress == null) {
            return true;
        }

        // Check for localhost
        if ("127.0.0.1".equals(ipAddress) || "::1".equals(ipAddress) || "localhost".equals(ipAddress)) {
            return true;
        }

        // Check for private IP ranges
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            return false; // Not a valid IPv4 address
        }

        try {
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);

            // 10.0.0.0/8
            if (first == 10) {
                return true;
            }

            // 172.16.0.0/12
            if (first == 172 && second >= 16 && second <= 31) {
                return true;
            }

            // 192.168.0.0/16
            if (first == 192 && second == 168) {
                return true;
            }

        } catch (NumberFormatException e) {
            return false;
        }

        return false;
    }

    /**
     * Clears the location cache
     */
    public void clearCache() {
        locationCache.clear();
        log.info("Location cache cleared");
    }

    /**
     * Gets cache size
     */
    public int getCacheSize() {
        return locationCache.size();
    }
}