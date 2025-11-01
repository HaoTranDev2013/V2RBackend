package com.v2r.v2rbackend.service.impl;

import com.v2r.v2rbackend.entity.OtpCode;
import com.v2r.v2rbackend.repository.OtpCodeRepository;
import com.v2r.v2rbackend.service.EmailService;
import com.v2r.v2rbackend.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class OtpServiceImpl implements OtpService {

    @Autowired
    private OtpCodeRepository otpCodeRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public String generateAndSendOtp(String email) {
        // Generate 5-digit OTP
        String otpCode = String.format("%05d", new Random().nextInt(100000));

        // Invalidate any existing unused OTPs for this email
        List<OtpCode> existingOtps = otpCodeRepository.findByEmailAndUsedFalse(email);
        for (OtpCode otp : existingOtps) {
            otp.setUsed(true);
            otpCodeRepository.save(otp);
        }

        // Create new OTP
        OtpCode otpCode1 = new OtpCode();
        otpCode1.setEmail(email);
        otpCode1.setOtpCode(otpCode);
        otpCode1.setCreatedAt(LocalDateTime.now());
        otpCode1.setExpiresAt(LocalDateTime.now().plusMinutes(2)); // OTP expires in 10 minutes
        otpCode1.setUsed(false);

        otpCodeRepository.save(otpCode1);

        // Send OTP via email
        emailService.sendOtpEmail(email, otpCode);

        return otpCode;
    }

    @Override
    public boolean verifyOtp(String email, String otpCode) {
        return otpCodeRepository.findByEmailAndOtpCodeAndUsedFalse(email, otpCode)
                .map(otp -> {
                    if (otp.getExpiresAt().isAfter(LocalDateTime.now())) {
                        // OTP is valid and not expired
                        otp.setUsed(true);
                        otpCodeRepository.save(otp);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
}
