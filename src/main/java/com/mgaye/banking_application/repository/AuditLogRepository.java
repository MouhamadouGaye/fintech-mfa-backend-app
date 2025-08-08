package com.mgaye.banking_application.repository;

import com.mgaye.banking_application.entity.AuditLog;
import com.mgaye.banking_application.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

        List<AuditLog> findByUserId(Long userId);

        List<AuditLog> findByAction(String action);

        List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

        @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
        Page<AuditLog> findAuditLogsByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.createdAt BETWEEN :startDate AND :endDate " +
                        "ORDER BY a.createdAt DESC")
        List<AuditLog> findUserAuditLogsByDateRange(@Param("userId") Long userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action LIKE %:actionPattern% " +
                        "AND a.createdAt >= :since")
        Long countActionsSince(@Param("actionPattern") String actionPattern, @Param("since") LocalDateTime since);

        List<AuditLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);

        @Query("SELECT a FROM AuditLog a WHERE a.action = :action AND a.timestamp > :since")
        List<AuditLog> findByActionSince(@Param("action") String action, @Param("since") LocalDateTime since);
}
