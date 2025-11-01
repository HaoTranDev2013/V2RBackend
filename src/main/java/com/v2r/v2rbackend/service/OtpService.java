package com.v2r.v2rbackend.service;

public interface OtpService {
    String generateAndSendOtp(String email);
    boolean verifyOtp(String email, String otpCode);
}
