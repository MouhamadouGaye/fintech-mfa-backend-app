package com.mgaye.banking_application.repository;

import com.mgaye.banking_application.dto.UserResponseDto;
import com.mgaye.banking_application.entity.KycStatus;
import com.mgaye.banking_application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// @Repository
// public interface UserRepository extends JpaRepository<User, Long> {

//     Optional<User> findByUsername(String username);

//     Optional<User> findByEmail(String email);

//     boolean existsByUsername(String username);

//     boolean existsByEmail(String email);

//     @Query("SELECT u FROM User u WHERE u.isActive = true")
//     List<User> findActiveUsers();

//     @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > :now")
//     List<User> findLockedUsers(@Param("now") LocalDateTime now);

//     @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
//     List<User> findUsersByCreationDateRange(@Param("startDate") LocalDateTime startDate,
//             @Param("endDate") LocalDateTime endDate);
// }

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

        Optional<User> findByUsername(String username);

        Optional<User> findById(Long id);

        List<User> findAll();

        Optional<User> findByEmail(String email);

        boolean existsByUsername(String username);

        boolean existsByEmail(String email);

        Optional<User> findByVerificationTokenIsNullAndIsVerifiedTrue();

        @Query("SELECT u FROM User u WHERE u.isActive = true")
        List<User> findActiveUsers();

        @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > :now")
        List<User> findLockedUsers(@Param("now") LocalDateTime now);

        @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
        List<User> findUsersByCreationDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        Optional<User> findByEmailIgnoreCase(String email);

        boolean existsByEmailIgnoreCase(String email);

        Optional<User> findByVerificationToken(String token);

        Optional<User> findByPasswordResetToken(String token);

        boolean existsByNationalId(String nationalId);

        @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.isActive = true AND u.isVerified = true")
        Optional<User> findActiveVerifiedUserByEmail(String email);

        @Query("SELECT u FROM User u WHERE u.accountLockedUntil < :now")
        List<User> findUsersWithExpiredLocks(@Param("now") LocalDateTime now);

        @Query("SELECT u FROM User u WHERE u.passwordExpiresAt < :now AND u.isActive = true")
        List<User> findUsersWithExpiredPasswords(@Param("now") LocalDateTime now);

        @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate AND u.isActive = true")
        List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

        @Query("SELECT u FROM User u WHERE u.kycStatus = :status")
        List<User> findByKycStatus(@Param("status") KycStatus status);

        @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt < :endDate")
        long countNewUsersInPeriod(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);
}
