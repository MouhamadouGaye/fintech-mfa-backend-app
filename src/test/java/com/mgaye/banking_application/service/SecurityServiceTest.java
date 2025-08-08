package com.mgaye.banking_application.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mgaye.banking_application.dto.Location;
import com.mgaye.banking_application.entity.AlertSeverity;
import com.mgaye.banking_application.entity.SecurityAlert;
import com.mgaye.banking_application.entity.SecurityAlertType;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.repository.SecurityAlertRepository;

// SecurityServiceTest.java
@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private SecurityAlertRepository securityAlertRepository;

    @Mock
    private GeoLocationService geoLocationService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SecurityService securityService;

    @Test
    void createAlert_WithHighSeverity_ShouldSendNotification() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        SecurityAlertType alertType = SecurityAlertType.SUSPICIOUS_LOGIN;
        AlertSeverity severity = AlertSeverity.HIGH;
        String message = "Suspicious login detected";
        String ipAddress = "192.168.1.1";

        // When
        securityService.createAlert(user, alertType, severity, message, ipAddress);

        // Then
        verify(securityAlertRepository).save(any(SecurityAlert.class));
        verify(notificationService).sendSecurityAlert(eq(user), any(SecurityAlert.class));
    }

    @Test
    void isUnusualLocation_WithDistantLocation_ShouldReturnTrue() {
        // Given
        User user = new User();
        user.setLastLoginIp("192.168.1.1");

        String currentIp = "203.0.113.1";

        Location lastLocation = new Location("New York", 40.7128, -74.0060);
        Location currentLocation = new Location("London", 51.5074, -0.1278);

        when(geoLocationService.getLocation(user.getLastLoginIp())).thenReturn(lastLocation);
        when(geoLocationService.getLocation(currentIp)).thenReturn(currentLocation);
        when(geoLocationService.calculateDistance(currentLocation, lastLocation)).thenReturn(5500.0);

        // When
        boolean isUnusual = securityService.isUnusualLocation(user, currentIp);

        // Then
        assertThat(isUnusual).isTrue();
    }
}
