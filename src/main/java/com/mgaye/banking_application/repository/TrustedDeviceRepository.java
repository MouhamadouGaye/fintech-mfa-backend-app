package com.mgaye.banking_application.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mgaye.banking_application.entity.TrustedDevice;
import com.mgaye.banking_application.entity.User;

@Repository
public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {

    Optional<TrustedDevice> findByUserAndDeviceIdAndIsActiveTrue(User user, String deviceId);

    Optional<TrustedDevice> findByUserAndDeviceFingerprintAndIsActiveTrue(User user, String deviceFingerprint,
            Boolean isActive);

    List<TrustedDevice> findByUserAndIsActiveTrueOrderByLastUsedAtDesc(User user);

    @Query("SELECT t FROM TrustedDevice t WHERE t.expiresAt < :now AND t.isActive = true")
    List<TrustedDevice> findExpiredDevices(@Param("now") LocalDateTime now);
}
