package com.mgaye.banking_application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private String ipAddress;
    private String country;
    private String countryCode;
    private String region;
    private String city;
    private Double latitude;
    private Double longitude;
    private String timezone;
    private String isp;
    private String organization;

    @Override
    public String toString() {
        return String.format("%s, %s, %s",
                city != null ? city : "Unknown",
                region != null ? region : "Unknown",
                country != null ? country : "Unknown");
    }
}