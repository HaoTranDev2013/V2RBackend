package com.v2r.v2rbackend.repository;

import com.v2r.v2rbackend.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Integer> {
    Optional<OtpCode> findByEmailAndOtpCodeAndUsedFalse(String email, String otpCode);
    List<OtpCode> findByEmailAndUsedFalse(String email);
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
