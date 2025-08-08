package com.mgaye.banking_application.service;

import com.mgaye.banking_application.entity.AuditAction;
import com.mgaye.banking_application.entity.AuditLog;
import com.mgaye.banking_application.entity.AuditStatus;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

// @Service
// @RequiredArgsConstructor
// public class AuditService {

//     private final AuditLogRepository auditLogRepository;

//     @Transactional(propagation = Propagation.REQUIRES_NEW)
//     public void logAction(String action, String entityType, Long entityId, String details) {
//         try {
//             AuditLog auditLog = new AuditLog();
//             auditLog.setAction(action);
//             auditLog.setEntityType(entityType);
//             auditLog.setEntityId(entityId);
//             auditLog.setDetails(details);

//             // Get current user information
//             Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//             if (authentication != null && authentication.isAuthenticated() &&
//                     !authentication.getPrincipal().equals("anonymousUser")) {
//                 auditLog.setUsername(authentication.getName());
//                 // Assuming UserDetailsImpl has getUserId method
//                 if (authentication
//                         .getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
//                     auditLog.setUsername(authentication.getName());
//                 }
//             }

//             // Get request information
//             ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
//             if (attr != null) {
//                 HttpServletRequest request = attr.getRequest();
//                 auditLog.setIpAddress(getClientIpAddress(request));
//                 auditLog.setUserAgent(request.getHeader("User-Agent"));
//             }

//             auditLogRepository.save(auditLog);
//         } catch (Exception e) {
//             // Log audit failures but don't throw exception to avoid breaking main flow
//             System.err.println("Failed to create audit log: " + e.getMessage());
//         }
//     }

//     @Transactional(readOnly = true)
//     public Page<AuditLog> getAuditLogs(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
//         if (startDate != null && endDate != null) {
//             return auditLogRepository.findAuditLogsByDateRange(startDate, endDate, pageable);
//         }
//         return auditLogRepository.findAll(pageable);
//     }

//     private String getClientIpAddress(HttpServletRequest request) {
//         String xForwardedFor = request.getHeader("X-Forwarded-For");
//         if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
//             return xForwardedFor.split(",")[0].trim();
//         }

//         String xRealIp = request.getHeader("X-Real-IP");
//         if (xRealIp != null && !xRealIp.isEmpty()) {
//             return xRealIp;
//         }

//         return request.getRemoteAddr();
//     }
// }

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(AuditAction action, User user, String ipAddress, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action.name());
        auditLog.setUser(user);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setStatus(AuditStatus.SUCCESS);
        auditLog.setDetails(details);

        auditLogRepository.save(auditLog);
        log.debug("Audit log created: {} for user: {}", action, user.getEmail());
    }

    public void logWithDetails(AuditAction action, User user, String ipAddress,
            String details, String oldValues, String newValues) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action.name());
        auditLog.setUser(user);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setStatus(AuditStatus.SUCCESS);
        auditLog.setDetails(details);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);

        auditLogRepository.save(auditLog);
    }

    public void logFailure(AuditAction action, String email, String ipAddress, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action.name());
        auditLog.setIpAddress(ipAddress);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setStatus(AuditStatus.FAILURE);
        auditLog.setDetails(details + " - Email: " + email);

        auditLogRepository.save(auditLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityType, Long entityId, String details) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setDetails(details);

            // Get current user information
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                    !authentication.getPrincipal().equals("anonymousUser")) {
                auditLog.setUsername(authentication.getName());
                // Assuming UserDetailsImpl has getUserId method
                if (authentication
                        .getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                    auditLog.setUsername(authentication.getName());
                }
            }

            // Get request information
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            if (attr != null) {
                HttpServletRequest request = attr.getRequest();
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Log audit failures but don't throw exception to avoid breaking main flow
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate != null && endDate != null) {
            return auditLogRepository.findAuditLogsByDateRange(startDate, endDate, pageable);
        }
        return auditLogRepository.findAll(pageable);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
