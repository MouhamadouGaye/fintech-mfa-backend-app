package com.mgaye.banking_application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
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

// ===============================================
// 2. Device Service - Fix DigestUtils issue
// ===============================================

@Slf4j
@Service
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

    private String generateDeviceFingerprint(HttpServletRequest request) {
        StringBuilder fingerprint = new StringBuilder();
        fingerprint.append(request.getHeader("User-Agent"));
        fingerprint.append(request.getHeader("Accept-Language"));
        fingerprint.append(request.getHeader("Accept-Encoding"));

        // Use MessageDigest instead of DigestUtils
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fingerprint.toString().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            // Fallback to simple hash
            return String.valueOf(fingerprint.toString().hashCode());
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()
                && !"unknown".equalsIgnoreCase(xForwardedForHeader)) {
            return xForwardedForHeader.split(",")[0].trim();
        }

        String xRealIpHeader = request.getHeader("X-Real-IP");
        if (xRealIpHeader != null && !xRealIpHeader.isEmpty() && !"unknown".equalsIgnoreCase(xRealIpHeader)) {
            return xRealIpHeader;
        }

        return request.getRemoteAddr();
    }

    public boolean isKnownDevice(User user, DeviceInfo deviceInfo) {
        return trustedDeviceRepository
                .findByUserAndDeviceFingerprintAndIsActive(user, deviceInfo.getDeviceName(), user.getIsActive())
                .isPresent();
    }

    public void sendDeviceVerificationCode(User user, DeviceInfo deviceInfo) {
        String code = generateVerificationCode();
        String key = "device_verification:" + user.getId() + ":" + deviceInfo.getDeviceId();

        // Store in Redis for 10 minutes
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(10));

        // Send email with verification code
        emailService.sendDeviceVerificationEmail(user, code, deviceInfo);
    }

    public boolean verifyDeviceCode(User user, String code) {
        String key = "device_verification:" + user.getId() + ":*";
        Set<String> keys = redisTemplate.keys(key);

        for (String redisKey : keys) {
            String storedCode = redisTemplate.opsForValue().get(redisKey);
            if (code.equals(storedCode)) {
                redisTemplate.delete(redisKey);
                return true;
            }
        }
        return false;
    }

    public void trustDevice(User user, DeviceInfo deviceInfo) {
        TrustedDevice device = TrustedDevice.builder()
                .user(user)
                .deviceFingerprint(deviceInfo.getDeviceId())
                .deviceName(generateDeviceName(deviceInfo))
                .deviceType(deviceInfo.getDeviceType())
                .browser(deviceInfo.getBrowser())
                .operatingSystem(deviceInfo.getOperatingSystem())
                .ipAddress(deviceInfo.getIpAddress())
                .isActive(true)
                .build();

        trustedDeviceRepository.save(device);
    }

    private String generateDeviceName(DeviceInfo deviceInfo) {
        return String.format("%s on %s", deviceInfo.getBrowser(), deviceInfo.getOperatingSystem());
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}