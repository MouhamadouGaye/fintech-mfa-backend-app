package com.mgaye.banking_application.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.entity.UserSession;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    Optional<UserSession> findBySessionIdAndUser(String sessionId, User user);

    List<UserSession> findByUserAndIsActiveTrueOrderByLastAccessedAtDesc(User user);

    @Query("SELECT s FROM UserSession s WHERE s.expiresAt < :now")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user = :user AND s.isActive = true")
    long countActiveSessionsForUser(@Param("user") User user);
}
