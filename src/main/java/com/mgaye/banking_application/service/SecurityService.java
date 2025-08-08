package com.mgaye.banking_application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.mgaye.banking_application.dto.Location;
import com.mgaye.banking_application.entity.AlertSeverity;
import com.mgaye.banking_application.entity.SecurityAlert;
import com.mgaye.banking_application.entity.SecurityAlertType;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.exception.AlertAlreadyAcknowledgedException;
import com.mgaye.banking_application.exception.SecurityAlertNotFoundException;
import com.mgaye.banking_application.repository.SecurityAlertRepository;
import com.mgaye.banking_application.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SecurityService {

    private final SecurityAlertRepository securityAlertRepository;
    private final GeoLocationService geoLocationService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final UserRepository userRepository;

    public SecurityService(SecurityAlertRepository securityAlertRepository,
            GeoLocationService geoLocationService, NotificationService notificationService,
            AuditService auditService, UserRepository userRepository) {
        this.securityAlertRepository = securityAlertRepository;
        this.geoLocationService = geoLocationService;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.userRepository = userRepository;
    }

    public boolean isUnusualLocation(User user, String ipAddress) {
        if (user.getLastLoginIp() == null)
            return false;

        Location currentLocation = geoLocationService.getLocation(ipAddress);
        Location lastLocation = geoLocationService.getLocation(user.getLastLoginIp());

        double distance = geoLocationService.calculateDistance(currentLocation, lastLocation);
        return distance > 100; // km
    }

    public void createAlert(User user, SecurityAlertType alertType, AlertSeverity severity,
            String message, String ipAddress) {
        SecurityAlert alert = new SecurityAlert();
        alert.setUser(user);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setIpAddress(ipAddress);
        alert.setCreatedAt(LocalDateTime.now());

        securityAlertRepository.save(alert);

        // Send notification for high/critical alerts
        if (severity == AlertSeverity.HIGH || severity == AlertSeverity.CRITICAL) {
            notificationService.sendSecurityAlert(user, alert);
        }
    }

    public void acknowledgeAlert(User user, Long alertId) {
        SecurityAlert alert = securityAlertRepository.findByIdAndUser(alertId, user);
        if (alert == null) {
            throw new SecurityAlertNotFoundException("Security alert not found");
        }

        if (alert.isAcknowledged()) {
            throw new AlertAlreadyAcknowledgedException("Alert already acknowledged");
        }

        alert.setAcknowledged(true);
        alert.setAcknowledgedAt(LocalDateTime.now());
        securityAlertRepository.save(alert);

        // Audit log
        auditService.logAction("SECURITY_ALERT_ACKNOWLEDGED", "SecurityAlert", alertId,
                String.format("Security alert acknowledged by user: %s", user.getUsername()));

        log.info("Security alert {} acknowledged by user: {}", alertId, user.getUsername());
    }
}
