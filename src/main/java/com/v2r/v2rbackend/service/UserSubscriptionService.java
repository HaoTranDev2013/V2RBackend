package com.v2r.v2rbackend.service;

import com.v2r.v2rbackend.dto.request.SubscriptionRegistrationRequest;
import com.v2r.v2rbackend.dto.response.UserSubscriptionResponse;
import com.v2r.v2rbackend.entity.UserSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionService {
    
    // Register user to subscription after payment
    UserSubscriptionResponse registerUserToSubscription(SubscriptionRegistrationRequest request);
    
    // Get all user subscriptions
    List<UserSubscription> findAll();
    Page<UserSubscription> findAll(Pageable pageable);
    
    // Get subscription by ID
    Optional<UserSubscription> findById(Integer id);
    
    // Get user's subscriptions
    List<UserSubscription> getUserSubscriptions(Integer userId);
    Page<UserSubscription> getUserSubscriptions(Integer userId, Pageable pageable);
    
    // Get user's active subscriptions
    List<UserSubscription> getUserActiveSubscriptions(Integer userId);
    Page<UserSubscription> getUserActiveSubscriptions(Integer userId, Pageable pageable);
    
    // Get current active subscription for user
    Optional<UserSubscription> getCurrentActiveSubscription(Integer userId);
    
    // Cancel subscription
    UserSubscription cancelSubscription(Integer subscriptionId);
    
    // Check if subscription is expired and update status
    void checkAndUpdateExpiredSubscriptions();
    
    // Get subscription details as response DTO
    UserSubscriptionResponse getSubscriptionResponse(Integer id);
    
    // Get effective model limit for user (including free tier)
    int getEffectiveModelLimit(Integer userId);
}
