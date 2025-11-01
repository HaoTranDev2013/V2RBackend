package com.v2r.v2rbackend.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otpCode);
}
