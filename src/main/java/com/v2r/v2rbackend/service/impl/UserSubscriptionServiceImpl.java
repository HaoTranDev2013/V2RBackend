package com.v2r.v2rbackend.service.impl;

import com.v2r.v2rbackend.dto.request.SubscriptionRegistrationRequest;
import com.v2r.v2rbackend.dto.response.UserSubscriptionResponse;
import com.v2r.v2rbackend.entity.Payment;
import com.v2r.v2rbackend.entity.Subscription;
import com.v2r.v2rbackend.entity.User;
import com.v2r.v2rbackend.entity.UserSubscription;
import com.v2r.v2rbackend.repository.SubscriptionRepository;
import com.v2r.v2rbackend.repository.UserRepository;
import com.v2r.v2rbackend.repository.UserSubscriptionRepository;
import com.v2r.v2rbackend.service.UserSubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private com.v2r.v2rbackend.config.SubscriptionConfig subscriptionConfig;

    @Override
    public UserSubscriptionResponse registerUserToSubscription(SubscriptionRegistrationRequest request) {
        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));

        // Validate subscription exists
        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with id: " + request.getSubscriptionId()));

        // Check if subscription is active
        if (!subscription.isStatus()) {
            throw new IllegalArgumentException("Subscription is not active");
        }

        // Validate duration
        if (request.getDurationMonths() == null || request.getDurationMonths() <= 0) {
            throw new IllegalArgumentException("Duration must be at least 1 month");
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setTransactionCode(request.getPaymentTransactionCode());
        payment.setAmount(request.getPaymentAmount() != null ? request.getPaymentAmount() : 0.0);
        payment.setStatus(true);
        payment.setPaymentDate(new Date());

        // Create user subscription
        UserSubscription userSubscription = new UserSubscription();
        userSubscription.setUser(user);
        userSubscription.setSubscription(subscription);
        userSubscription.setPayment(payment);
        
        LocalDateTime now = LocalDateTime.now();
        userSubscription.setStartDate(now);
        userSubscription.setEndDate(now.plusMonths(request.getDurationMonths()));
        userSubscription.setActive(true);

        // Save subscription
        UserSubscription savedSubscription = userSubscriptionRepository.save(userSubscription);

        // Update user's numberOfModel based on subscription
        // ADD subscription models to user's existing models
        if (subscription.getNumberOfModel() != null) {
            if (subscription.getNumberOfModel() == -1) {
                // Unlimited models - set to unlimited
                user.setNumberOfModel(-1);
            } else {
                // Add subscription models to current models
                int currentModels = user.getNumberOfModel();
                int newTotalModels = currentModels + subscription.getNumberOfModel();
                user.setNumberOfModel(newTotalModels);
            }
            userRepository.save(user);
        }

        return convertToResponse(savedSubscription);
    }

    @Override
    public List<UserSubscription> findAll() {
        return userSubscriptionRepository.findAll();
    }

    @Override
    public Page<UserSubscription> findAll(Pageable pageable) {
        return userSubscriptionRepository.findAll(pageable);
    }

    @Override
    public Optional<UserSubscription> findById(Integer id) {
        return userSubscriptionRepository.findById(id);
    }

    @Override
    public List<UserSubscription> getUserSubscriptions(Integer userId) {
        return userSubscriptionRepository.findByUser_UserID(userId);
    }

    @Override
    public Page<UserSubscription> getUserSubscriptions(Integer userId, Pageable pageable) {
        return userSubscriptionRepository.findByUser_UserID(userId, pageable);
    }

    @Override
    public List<UserSubscription> getUserActiveSubscriptions(Integer userId) {
        return userSubscriptionRepository.findByUser_UserIDAndActive(userId, true);
    }

    @Override
    public Page<UserSubscription> getUserActiveSubscriptions(Integer userId, Pageable pageable) {
        return userSubscriptionRepository.findByUser_UserIDAndActive(userId, true, pageable);
    }

    @Override
    public Optional<UserSubscription> getCurrentActiveSubscription(Integer userId) {
        return userSubscriptionRepository.findByUser_UserIDAndActiveAndEndDateAfter(userId, true, LocalDateTime.now());
    }

    @Override
    public UserSubscription cancelSubscription(Integer subscriptionId) {
        UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("User subscription not found with id: " + subscriptionId));

        subscription.setActive(false);
        userSubscriptionRepository.save(subscription);

        // Restore free tier when subscription is cancelled
        User user = subscription.getUser();
        user.setNumberOfModel(subscriptionConfig.getFreeModelLimit());
        userRepository.save(user);

        return subscription;
    }

    @Override
    public void checkAndUpdateExpiredSubscriptions() {
        List<UserSubscription> expiredSubscriptions = 
            userSubscriptionRepository.findByActiveAndEndDateBefore(true, LocalDateTime.now());

        for (UserSubscription subscription : expiredSubscriptions) {
            subscription.setActive(false);
            userSubscriptionRepository.save(subscription);

            // Restore free tier when subscription expires
            User user = subscription.getUser();
            user.setNumberOfModel(subscriptionConfig.getFreeModelLimit());
            userRepository.save(user);
        }
    }

    @Override
    public UserSubscriptionResponse getSubscriptionResponse(Integer id) {
        UserSubscription subscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User subscription not found with id: " + id));

        return convertToResponse(subscription);
    }

    /**
     * Get the effective model limit for a user
     * If user has active subscription, return that limit
     * If user has no subscription, return free tier limit
     */
    public int getEffectiveModelLimit(Integer userId) {
        Optional<UserSubscription> activeSubscription = getCurrentActiveSubscription(userId);
        
        if (activeSubscription.isPresent()) {
            Integer modelLimit = activeSubscription.get().getSubscription().getNumberOfModel();
            return modelLimit != null ? modelLimit : subscriptionConfig.getFreeModelLimit();
        }
        
        // No active subscription - return free tier
        return subscriptionConfig.getFreeModelLimit();
    }

    private UserSubscriptionResponse convertToResponse(UserSubscription subscription) {
        UserSubscriptionResponse response = new UserSubscriptionResponse();
        response.setId(subscription.getId());
        
        if (subscription.getUser() != null) {
            response.setUserId(subscription.getUser().getUserID());
            response.setUserEmail(subscription.getUser().getEmail());
            response.setUserFullName(subscription.getUser().getFullName());
        }
        
        if (subscription.getSubscription() != null) {
            response.setSubscriptionId(subscription.getSubscription().getId());
            response.setSubscriptionName(subscription.getSubscription().getName());
            response.setSubscriptionPrice(subscription.getSubscription().getPrice());
            response.setNumberOfModel(subscription.getSubscription().getNumberOfModel());
        }
        
        response.setStartDate(subscription.getStartDate());
        response.setEndDate(subscription.getEndDate());
        response.setActive(subscription.isActive());
        
        // Calculate days remaining
        if (subscription.isActive() && subscription.getEndDate() != null) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getEndDate());
            response.setDaysRemaining(Math.max(0, daysRemaining));
        } else {
            response.setDaysRemaining(0L);
        }
        
        return response;
    }
}
