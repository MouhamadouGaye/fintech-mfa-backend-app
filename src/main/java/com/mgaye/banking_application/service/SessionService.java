package com.mgaye.banking_application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.entity.UserSession;
import com.mgaye.banking_application.repository.UserSessionRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SessionService {

    private final UserSessionRepository sessionRepository;
    // private final RedisTemplate<String, String> redisTemplate;

    public SessionService(UserSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;

    }

    public UserSession createSession(User user, HttpServletRequest request, String token) {
        // Check concurrent session limit
        long activeSessions = sessionRepository.countActiveSessionsForUser(user);
        if (activeSessions >= user.getMaxConcurrentSessions()) {
            // Invalidate oldest session
            List<UserSession> sessions = sessionRepository
                    .findByUserAndIsActiveTrueOrderByLastAccessedAtDesc(user);
            UserSession oldest = sessions.get(sessions.size() - 1);
            oldest.setIsActive(false);
            sessionRepository.save(oldest);
        }

        String sessionId = UUID.randomUUID().toString();
        UserSession session = new UserSession();
        session.setSessionId(sessionId);
        session.setUser(user);
        session.setIpAddress(getClientIpAddress(request));
        session.setUserAgent(request.getHeader("User-Agent"));
        session.setCreatedAt(LocalDateTime.now());
        session.setLastAccessedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(user.getSessionTimeoutMinutes()));

        return sessionRepository.save(session);
    }

    public void updateSessionActivity(String sessionId) {
        Optional<UserSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setLastAccessedAt(LocalDateTime.now());
            sessionRepository.save(session);
        }
    }

    public void invalidateSession(String sessionId) {
        Optional<UserSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setIsActive(false);
            sessionRepository.save(session);
        }
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

    public boolean isValidSession(User user, String sessionId) {
        Optional<UserSession> session = sessionRepository.findBySessionIdAndUser(sessionId, user);
        if (session.isPresent()) {
            UserSession userSession = session.get();
            return userSession.getIsActive() &&
                    userSession.getExpiresAt().isAfter(LocalDateTime.now());
        }
        return false;
    }

    public void updateSession(User user, String sessionId, String accessToken, String refreshToken) {
        Optional<UserSession> sessionOpt = sessionRepository.findBySessionIdAndUser(sessionId, user);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setLastAccessedAt(LocalDateTime.now());
            // You might want to store tokens in session if needed
            sessionRepository.save(session);
        }
    }

    public void invalidateSession(User user, String sessionId) {
        Optional<UserSession> sessionOpt = sessionRepository.findBySessionIdAndUser(sessionId, user);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setIsActive(false);
            session.setExpiresAt(LocalDateTime.now());
            sessionRepository.save(session);
        }
    }

    public void invalidateAllUserSessions(User user) {
        List<UserSession> activeSessions = sessionRepository
                .findByUserAndIsActiveTrueOrderByLastAccessedAtDesc(user);
        activeSessions.forEach(session -> {
            session.setIsActive(false);
            session.setExpiresAt(LocalDateTime.now());
        });
        sessionRepository.saveAll(activeSessions);
    }

}