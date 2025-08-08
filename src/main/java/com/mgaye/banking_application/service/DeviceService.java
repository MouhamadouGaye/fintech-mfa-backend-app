package com.mgaye.banking_application.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import eu.bitwalker.useragentutils.UserAgent;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.mgaye.banking_application.dto.DeviceInfo;
import com.mgaye.banking_application.entity.TrustedDevice;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.repository.TrustedDeviceRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceService {

    private final TrustedDeviceRepository trustedDeviceRepository;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    public DeviceInfo extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(request);

        UserAgent ua = UserAgent.parseUserAgentString(userAgent);

        return DeviceInfo.builder()
                .deviceId(generateDeviceFingerprint(request))
                .browser(ua.getBrowser().getName())
                .operatingSystem(ua.getOperatingSystem().getName())
                .deviceType(ua.getOperatingSystem().getDeviceType().getName())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    public boolean isKnownDevice(User user, DeviceInfo deviceInfo) {
        return trustedDeviceRepository
                .findByUserAndDeviceIdAndIsActiveTrue(user, deviceInfo.getDeviceId())
                .isPresent();
    }

    public void sendDeviceVerificationCode(User user, DeviceInfo deviceInfo) {
        String code = RandomStringUtils.randomNumeric(6);
        String key = "device_verification:" + user.getId() + ":" + deviceInfo.getDeviceId();

        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(10));
        emailService.sendDeviceVerificationEmail(user, code, deviceInfo);
    }

    public boolean verifyDeviceCode(User user, String code) {
        String key = "device_verification:" + user.getId() + ":*";
        Set<String> keys = redisTemplate.keys(key);

        for (String k : keys) {
            String storedCode = redisTemplate.opsForValue().get(k);
            if (code.equals(storedCode)) {
                redisTemplate.delete(k);
                return true;
            }
        }
        return false;
    }

    public void trustDevice(User user, DeviceInfo deviceInfo) {
        TrustedDevice device = new TrustedDevice();
        device.setUser(user);
        device.setDeviceId(deviceInfo.getDeviceId());
        device.setDeviceName(generateDeviceName(deviceInfo));
        device.setDeviceType(deviceInfo.getDeviceType());
        device.setBrowser(deviceInfo.getBrowser());
        device.setOperatingSystem(deviceInfo.getOperatingSystem());
        device.setIpAddress(deviceInfo.getIpAddress());
        device.setTrustedAt(LocalDateTime.now());
        device.setLastUsedAt(LocalDateTime.now());
        device.setExpiresAt(LocalDateTime.now().plusDays(90));

        trustedDeviceRepository.save(device);
    }

    private String generateDeviceFingerprint(HttpServletRequest request) {
        StringBuilder fingerprint = new StringBuilder();
        fingerprint.append(request.getHeader("User-Agent"));
        fingerprint.append(request.getHeader("Accept-Language"));
        fingerprint.append(request.getHeader("Accept-Encoding"));

        return DigestUtils.sha256Hex(fingerprint.toString());
    }

    private String generateDeviceName(DeviceInfo deviceInfo) {
        return deviceInfo.getBrowser() + " on " + deviceInfo.getOperatingSystem();
    }

    // 1. Create the missing getClientIpAddress utility method
    // Add this as a private method to your SessionService, DeviceService, and
    // AuthService classes

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()
                && !"unknown".equalsIgnoreCase(xForwardedForHeader)) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedForHeader.split(",")[0].trim();
        }

        String xRealIpHeader = request.getHeader("X-Real-IP");
        if (xRealIpHeader != null && !xRealIpHeader.isEmpty() && !"unknown".equalsIgnoreCase(xRealIpHeader)) {
            return xRealIpHeader;
        }

        String xForwardedHeader = request.getHeader("X-Forwarded");
        if (xForwardedHeader != null && !xForwardedHeader.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedHeader)) {
            return xForwardedHeader;
        }

        String forwardedForHeader = request.getHeader("Forwarded-For");
        if (forwardedForHeader != null && !forwardedForHeader.isEmpty()
                && !"unknown".equalsIgnoreCase(forwardedForHeader)) {
            return forwardedForHeader;
        }

        String forwardedHeader = request.getHeader("Forwarded");
        if (forwardedHeader != null && !forwardedHeader.isEmpty() && !"unknown".equalsIgnoreCase(forwardedHeader)) {
            return forwardedHeader;
        }

        return request.getRemoteAddr();
    }
}
