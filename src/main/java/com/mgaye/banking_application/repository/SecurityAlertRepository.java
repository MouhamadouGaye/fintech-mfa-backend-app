package com.mgaye.banking_application.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mgaye.banking_application.entity.AlertSeverity;
import com.mgaye.banking_application.entity.SecurityAlert;
import com.mgaye.banking_application.entity.User;

@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {

    List<SecurityAlert> findByUserAndAcknowledgedFalseOrderByCreatedAtDesc(User user);

    SecurityAlert findByIdAndUser(Long id, User user);

    @Query("SELECT s FROM SecurityAlert s WHERE s.severity = :severity AND s.acknowledged = false")
    List<SecurityAlert> findUnacknowledgedBySeverity(@Param("severity") AlertSeverity severity);

    @Query("SELECT COUNT(s) FROM SecurityAlert s WHERE s.user = :user AND s.createdAt > :since")
    long countAlertsForUserSince(@Param("user") User user, @Param("since") LocalDateTime since);
}
