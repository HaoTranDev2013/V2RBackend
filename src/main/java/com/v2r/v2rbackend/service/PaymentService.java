package com.v2r.v2rbackend.service;

import com.v2r.v2rbackend.config.VNPayConfig;
import com.v2r.v2rbackend.dto.PaymentDTO;
import com.v2r.v2rbackend.entity.*;
import com.v2r.v2rbackend.repository.*;
import com.v2r.v2rbackend.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private VNPayConfig vnPayConfig;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create VNPay payment URL for subscription purchase
     */
    public PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest request, Integer subscriptionId, User user) {
        // Get subscription
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + subscriptionId));

        // Parse amount from subscription price (remove dots and commas, convert to VND cents)
        String priceStr = subscription.getPrice().replace(".", "").replace(",", "");
        long amount = Long.parseLong(priceStr) * 100; // VNPay requires amount * 100

        // Get VNPay configuration parameters
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig(true);
        
        // Set amount
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        
        // Set bank code if provided
        String bankCode = request.getParameter("bankCode");
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        
        // Set IP address
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        
        // Set order info with user email for callback processing
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan goi " + subscription.getName() + " - User: " + user.getEmail());
        
        // Build payment URL with signature
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnpUrl() + "?" + queryUrl;

        return PaymentDTO.VNPayResponse.builder()
                .code("00")
                .message("success")
                .paymentUrl(paymentUrl)
                .build();
    }

    /**
     * Process VNPay payment callback and create all necessary records
     */
    @Transactional
    public void processPaymentCallback(Map<String, String> params, User user) {
        String responseCode = params.get("vnp_ResponseCode");
        
        if (!"00".equals(responseCode)) {
            throw new RuntimeException("Payment failed with response code: " + responseCode);
        }
        
        // Payment successful - extract data
        String vnpTxnRef = params.get("vnp_TxnRef");
        String vnpTransactionNo = params.get("vnp_TransactionNo");
        String amountStr = params.get("vnp_Amount");
        String orderInfo = params.get("vnp_OrderInfo");
        
        System.out.println("Processing payment callback:");
        System.out.println("- TxnRef: " + vnpTxnRef);
        System.out.println("- TransactionNo: " + vnpTransactionNo);
        System.out.println("- Amount: " + amountStr);
        System.out.println("- OrderInfo: " + orderInfo);
        
        // Convert amount back from VND cents
        long amount = Long.parseLong(amountStr) / 100;
        
        // Extract subscription name from order info
        String subscriptionName = extractSubscriptionName(orderInfo);
        Subscription subscription = subscriptionRepository.findByName(subscriptionName)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionName));
        
        // Fetch managed user entity from database
        User managedUser = userRepository.findById(user.getUserID())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + user.getUserID()));
        
        // Step 1: Create Order
        Order order = new Order();
        order.setUser(managedUser);
        order.setOrder_date(new Date());
        order.setTotalPrice(amount);
        order.setStatus(1); // 1 = Paid
        order = orderRepository.save(order);
        System.out.println("Order created with ID: " + order.getOrderID());
        
        // Step 2: Create OrderDetail
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setSubscription(subscription);
        orderDetail.setQuantity(1); // 1 month duration
        orderDetail.setPricePerUnit(amount);
        orderDetail.setTotalPrice(amount);
        orderDetailRepository.save(orderDetail);
        System.out.println("OrderDetail created");
        
        // Step 3: Create Payment record
        Payment payment = new Payment();
        payment.setTransactionCode(vnpTransactionNo);
        payment.setAmount(amount);
        payment.setStatus(true); // true = Success
        payment.setPaymentDate(new Date());
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        System.out.println("Payment record created with ID: " + payment.getPaymentID());
        
        // Step 4: Update user subscription
        updateUserSubscription(managedUser, subscription, payment);
        System.out.println("Payment processing completed successfully");
    }

    /**
     * Extract subscription name from VNPay order info
     * Format: "Thanh toan goi {SubscriptionName} - User: {email}"
     */
    private String extractSubscriptionName(String orderInfo) {
        if (orderInfo == null || !orderInfo.contains("Thanh toan goi ")) {
            throw new RuntimeException("Invalid order info format");
        }
        
        String[] parts = orderInfo.split("Thanh toan goi ");
        if (parts.length < 2) {
            throw new RuntimeException("Cannot extract subscription name from order info");
        }
        
        String[] nameParts = parts[1].split(" - User:");
        if (nameParts.length < 1) {
            throw new RuntimeException("Cannot extract subscription name from order info");
        }
        
        return nameParts[0].trim();
    }

    /**
     * Update user subscription after successful payment
     * - Deactivates old subscription if exists
     * - Creates new subscription with 1 month duration
     * - Updates user's numberOfModel based on subscription plan
     */
    private void updateUserSubscription(User user, Subscription subscription, Payment payment) {
        // Check if user has active subscription and deactivate it
        UserSubscription existingSub = userSubscriptionRepository.findByUserAndActiveTrue(user)
                .orElse(null);

        if (existingSub != null) {
            existingSub.setActive(false);
            userSubscriptionRepository.save(existingSub);
            System.out.println("Deactivated previous subscription ID: " + existingSub.getId());
        }

        // Create new subscription with 1 month duration
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(1);

        UserSubscription userSubscription = new UserSubscription();
        userSubscription.setUser(user);
        userSubscription.setSubscription(subscription);
        userSubscription.setPayment(payment);
        userSubscription.setStartDate(startDate);
        userSubscription.setEndDate(endDate);
        userSubscription.setActive(true);
        userSubscriptionRepository.save(userSubscription);
        System.out.println("Created new user subscription ID: " + userSubscription.getId());

        // Update user's number of models
        if (subscription.getNumberOfModel() != null) {
            int currentModels = user.getNumberOfModel();
            int newModelCount;
            
            if (subscription.getNumberOfModel() == -1) {
                // Unlimited subscription
                newModelCount = Integer.MAX_VALUE;
                System.out.println("User upgraded to unlimited models");
            } else {
                // Regular subscription - add models
                newModelCount = currentModels + subscription.getNumberOfModel();
                System.out.println("User models increased from " + currentModels + " to " + newModelCount);
            }
            
            user.setNumberOfModel(newModelCount);
            userRepository.save(user);
        }
    }
}
