package com.mgaye.banking_application.security;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mgaye.banking_application.entity.TrustedDevice;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.entity.UserSession;
import com.mgaye.banking_application.repository.LoginAttemptRepository;
import com.mgaye.banking_application.repository.TrustedDeviceRepository;
import com.mgaye.banking_application.repository.UserRepository;
import com.mgaye.banking_application.repository.UserSessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityMaintenanceTasks {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final TrustedDeviceRepository trustedDeviceRepository;
    private final LoginAttemptRepository loginAttemptRepository;

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupExpiredSessions() {
        List<UserSession> expiredSessions = sessionRepository.findExpiredSessions(LocalDateTime.now());
        expiredSessions.forEach(session -> session.setIsActive(false));
        sessionRepository.saveAll(expiredSessions);

        if (!expiredSessions.isEmpty()) {
            log.info("Cleaned up {} expired sessions", expiredSessions.size());
        }
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    public void unlockExpiredAccounts() {
        List<User> lockedUsers = userRepository.findUsersWithExpiredLocks(LocalDateTime.now());
        lockedUsers.forEach(user -> {
            user.setAccountLockedUntil(null);
            user.setFailedLoginAttempts(0);
        });
        userRepository.saveAll(lockedUsers);

        if (!lockedUsers.isEmpty()) {
            log.info("Unlocked {} accounts with expired locks", lockedUsers.size());
        }
    }

    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupExpiredDevices() {
        List<TrustedDevice> expiredDevices = trustedDeviceRepository.findExpiredDevices(LocalDateTime.now());
        expiredDevices.forEach(device -> device.setIsActive(false));
        trustedDeviceRepository.saveAll(expiredDevices);

        if (!expiredDevices.isEmpty()) {
            log.info("Cleaned up {} expired trusted devices", expiredDevices.size());
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1 AM
    public void alertPasswordExpirations() {
        List<User> usersWithExpiredPasswords = userRepository
                .findUsersWithExpiredPasswords(LocalDateTime.now().plusDays(7));

        // Send password expiration warnings
        usersWithExpiredPasswords.forEach(user -> {
            user.setForcePasswordChange(true);
        });
        userRepository.saveAll(usersWithExpiredPasswords);

        if (!usersWithExpiredPasswords.isEmpty()) {
            log.info("Marked {} users for password change due to expiration",
                    usersWithExpiredPasswords.size());
        }
    }
}
