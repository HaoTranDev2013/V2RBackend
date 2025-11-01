package com.v2r.v2rbackend.repository;

import com.v2r.v2rbackend.entity.User;
import com.v2r.v2rbackend.entity.UserSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer> {
    
    // Find all subscriptions for a user
    List<UserSubscription> findByUser_UserID(Integer userId);
    Page<UserSubscription> findByUser_UserID(Integer userId, Pageable pageable);
    
    // Find active subscriptions for a user
    List<UserSubscription> findByUser_UserIDAndActive(Integer userId, boolean active);
    Page<UserSubscription> findByUser_UserIDAndActive(Integer userId, boolean active, Pageable pageable);
    
    // Find current active subscription for a user
    Optional<UserSubscription> findByUser_UserIDAndActiveAndEndDateAfter(Integer userId, boolean active, LocalDateTime currentDate);
    
    // Find active subscription for a user (any active)
    Optional<UserSubscription> findByUserAndActiveTrue(User user);
    
    // Find all active subscriptions
    List<UserSubscription> findByActive(boolean active);
    Page<UserSubscription> findByActive(boolean active, Pageable pageable);
    
    // Find subscriptions by subscription plan
    List<UserSubscription> findBySubscriptionId(Integer subscriptionId);
    Page<UserSubscription> findBySubscriptionId(Integer subscriptionId, Pageable pageable);
    
    // Find expired subscriptions
    List<UserSubscription> findByActiveAndEndDateBefore(boolean active, LocalDateTime currentDate);
}
