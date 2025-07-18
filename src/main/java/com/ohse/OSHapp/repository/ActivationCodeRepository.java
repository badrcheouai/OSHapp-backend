package com.ohse.OSHapp.repository;

import com.ohse.OSHapp.model.ActivationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.time.LocalDateTime;

public interface ActivationCodeRepository extends JpaRepository<ActivationCode, Long> {
    Optional<ActivationCode> findByCode(String code);
    Optional<ActivationCode> findByEmail(String email);
    @Modifying
    @Transactional
    void deleteByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO activation_code (email, code, expiry_date, used) VALUES (:email, :code, :expiryDate, false) " +
            "ON CONFLICT (email) DO UPDATE SET code = EXCLUDED.code, expiry_date = EXCLUDED.expiry_date, used = false", nativeQuery = true)
    void upsertActivationCode(@Param("email") String email, @Param("code") String code, @Param("expiryDate") LocalDateTime expiryDate);
} 