package com.mgaye.banking_application.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mgaye.banking_application.entity.LoginAttempt;
import com.mgaye.banking_application.entity.User;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    @Query("SELECT COUNT(l) FROM LoginAttempt l WHERE l.ipAddress = :ip AND l.attemptTime > :since AND l.success = false")
    long countFailedAttemptsByIpSince(@Param("ip") String ipAddress, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(l) FROM LoginAttempt l WHERE l.email = :email AND l.attemptTime > :since AND l.success = false")
    long countFailedAttemptsByEmailSince(@Param("email") String email, @Param("since") LocalDateTime since);

    List<LoginAttempt> findByUserAndAttemptTimeAfterOrderByAttemptTimeDesc(User user, LocalDateTime after);
}
