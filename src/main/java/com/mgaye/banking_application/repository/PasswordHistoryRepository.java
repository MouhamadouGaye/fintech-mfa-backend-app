package com.mgaye.banking_application.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.mgaye.banking_application.entity.PasswordHistory;
import com.mgaye.banking_application.entity.User;

import io.lettuce.core.dynamic.annotation.Param;

// @Repository
// public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
//     List<PasswordHistory> findTop5ByUserOrderByChangedAtDesc(User user);
// }

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    /**
     * Find the last N password history entries for a user, ordered by most recent
     * first
     */
    List<PasswordHistory> findTop5ByUserOrderByChangedAtDesc(User user);

    /**
     * Find password history entries for a user within a date range
     */
    List<PasswordHistory> findByUserAndChangedAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all password history entries for a user
     */
    List<PasswordHistory> findByUserOrderByChangedAtDesc(User user);

    /**
     * Count password changes for a user within a time period
     */
    @Query("SELECT COUNT(ph) FROM PasswordHistory ph WHERE ph.user = :user AND ph.changedAt >= :since")
    long countPasswordChangesSince(@Param("user") User user, @Param("since") LocalDateTime since);

    /**
     * Find password history entries by change reason
     */
    List<PasswordHistory> findByUserAndChangeReasonOrderByChangedAtDesc(User user, String changeReason);

    /**
     * Delete old password history entries (for cleanup)
     */
    @Query("DELETE FROM PasswordHistory ph WHERE ph.changedAt < :cutoffDate")
    void deleteOldEntries(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find the most recent password change for a user
     */
    PasswordHistory findFirstByUserOrderByChangedAtDesc(User user);

    /**
     * Check if user has changed password recently
     */
    @Query("SELECT COUNT(ph) > 0 FROM PasswordHistory ph WHERE ph.user = :user AND ph.changedAt >= :since")
    boolean hasChangedPasswordSince(@Param("user") User user, @Param("since") LocalDateTime since);
}