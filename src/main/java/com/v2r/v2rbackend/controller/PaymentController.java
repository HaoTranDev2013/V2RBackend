package com.v2r.v2rbackend.controller;

import com.v2r.v2rbackend.config.VNPayConfig;
import com.v2r.v2rbackend.dto.PaymentDTO;
import com.v2r.v2rbackend.entity.User;
import com.v2r.v2rbackend.repository.UserRepository;
import com.v2r.v2rbackend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payment", description = "Payment management APIs")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VNPayConfig vnPayConfig;

    @Value("${APP_DOMAIN:http://localhost:3000}")
    private String domain;

    @PostMapping("/vn-pay")
    @Operation(summary = "Create VNPay payment URL", description = "Generate payment URL for VNPay gateway")
    public ResponseEntity<PaymentDTO.VNPayResponse> createPayment(
            HttpServletRequest request,
            @RequestParam Integer subscriptionId,
            @RequestParam(required = false) String bankCode,
            @RequestParam(required = false) String email // allow passing email when security is disabled
    ) {
        // Try to get email from authentication if present
        if (email == null || email.isBlank()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                email = authentication.getName();
            }
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required to create payment");
        }

        final String lookupEmail = email; // make effectively final for lambda usage

        // Fetch user entity from database
        User user = userRepository.findByEmail(lookupEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + lookupEmail));

        PaymentDTO.VNPayResponse response = paymentService.createVnPayPayment(request, subscriptionId, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vn-pay-callback")
    @Operation(summary = "VNPay payment callback", description = "Handle VNPay payment callback")
    public void vnPayCallback(
            @RequestParam Map<String, String> params,
            HttpServletResponse response) throws IOException {

        String responseCode = params.get("vnp_ResponseCode");

        if ("00".equals(responseCode)) {
            // Payment successful - need to get user from transaction
            String paymentCode = params.get("vnp_TransactionNo");
            response.sendRedirect(domain + "/payment-success?paymentCode=" + paymentCode);
        } else {
            // Payment failed
            response.sendRedirect(domain + "/payment-fail?paymentStatus=0");
        }
    }

    @GetMapping("/vnpay-return")
    @Operation(summary = "VNPay return URL", description = "Process VNPay return after payment")
    public void vnpayReturn(
            @RequestParam Map<String, String> params,
            HttpServletResponse response) throws IOException {

        String responseCode = params.get("vnp_ResponseCode");
        String orderInfo = params.get("vnp_OrderInfo");
        String vnpSecureHash = params.get("vnp_SecureHash");

        System.out.println("=== VNPay Payment Return ===");
        System.out.println("Response Code: " + responseCode);
        System.out.println("Order Info: " + orderInfo);

        try {
            // Step 1: Validate signature
            Map<String, String> fields = new java.util.HashMap<>(params);
            fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            String hashData = com.v2r.v2rbackend.util.VNPayUtil.getPaymentURL(fields, false);
            String calculatedHash = com.v2r.v2rbackend.util.VNPayUtil.hmacSHA512(
                    vnPayConfig.getVnpHashSecret(),
                    hashData
            );

            System.out.println("VNPay Hash: " + vnpSecureHash);
            System.out.println("Calculated Hash: " + calculatedHash);

            if (!calculatedHash.equals(vnpSecureHash)) {
                System.err.println("Invalid signature! Payment may be tampered.");
                response.sendRedirect(domain + "/payment-fail?error=invalid_signature");
                return;
            }

            if (!"00".equals(responseCode)) {
                System.err.println("Payment failed with response code: " + responseCode);
                response.sendRedirect(domain + "/payment-fail?paymentStatus=" + responseCode);
                return;
            }

            // Step 3: Extract user email from order info
            String emailFromInfo = extractUserEmail(orderInfo);
            System.out.println("Extracted email: " + emailFromInfo);

            User user = userRepository.findByEmail(emailFromInfo)
                    .orElseThrow(() -> new RuntimeException("User not found: " + emailFromInfo));

            // Step 4: Process payment and create records
            paymentService.processPaymentCallback(params, user);

            String paymentCode = params.get("vnp_TransactionNo");
            System.out.println("Payment processed successfully: " + paymentCode);
            response.sendRedirect(domain + "/payment-success?paymentCode=" + paymentCode);

        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            response.sendRedirect(domain + "/payment-fail?error=" + java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private String extractUserEmail(String orderInfo) {
        // Extract email from order info like "Thanh toan goi Basic - User: test@example.com"
        if (orderInfo != null && orderInfo.contains("User: ")) {
            String[] parts = orderInfo.split("User: ");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        throw new RuntimeException("Cannot extract user email from order info");
    }

    @PostMapping("/vnpay-ipn")
    @Operation(summary = "VNPay IPN (Instant Payment Notification)", description = "Handle VNPay IPN callback")
    public ResponseEntity<Map<String, String>> vnpayIPN(@RequestParam Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");
        String secureHash = params.get("vnp_SecureHash");

        // Remove secure hash from params for validation
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        // TODO: Add signature validation and payment processing

        if ("00".equals(responseCode)) {
            return ResponseEntity.ok(Map.of(
                    "RspCode", "00",
                    "Message", "Confirm Success"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "RspCode", "01",
                    "Message", "Order not found"
            ));
        }
    }
}