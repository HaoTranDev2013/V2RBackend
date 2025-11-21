package com.v2r.v2rbackend.controller;

import com.v2r.v2rbackend.dto.request.SubscriptionRegistrationRequest;
import com.v2r.v2rbackend.dto.response.UserSubscriptionResponse;
import com.v2r.v2rbackend.entity.UserSubscription;
import com.v2r.v2rbackend.service.UserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user-subscriptions")
@Tag(name = "User Subscription", description = "User subscription management endpoints - Register users to subscription plans after payment")
public class UserSubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(UserSubscriptionController.class);

    @Autowired
    private UserSubscriptionService userSubscriptionService;

    @PostMapping("/register")
    @Operation(summary = "Register user to subscription after payment", 
               description = "Register a user to a subscription plan after payment is completed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered to subscription"),
            @ApiResponse(responseCode = "400", description = "Invalid input or subscription inactive"),
            @ApiResponse(responseCode = "404", description = "User or subscription not found")
    })
    public ResponseEntity<?> registerUserToSubscription(@RequestBody SubscriptionRegistrationRequest request) {
        try {
            UserSubscriptionResponse response = userSubscriptionService.registerUserToSubscription(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registering user to subscription: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Get all user subscriptions with pagination", 
               description = "Retrieve all user subscriptions with pagination support")
    public ResponseEntity<Page<UserSubscription>> getAllUserSubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserSubscription> subscriptions = userSubscriptionService.findAll(pageable);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user subscription by ID", 
               description = "Retrieve a specific user subscription by its ID")
    public ResponseEntity<?> getUserSubscriptionById(@PathVariable Integer id) {
        try {
            UserSubscriptionResponse response = userSubscriptionService.getSubscriptionResponse(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving subscription: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all subscriptions for a user with pagination", 
               description = "Retrieve all subscriptions (active and inactive) for a specific user")
    public ResponseEntity<Page<UserSubscription>> getUserSubscriptions(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserSubscription> subscriptions = userSubscriptionService.getUserSubscriptions(userId, pageable);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/active")
    @Operation(summary = "Get active subscriptions for a user with pagination", 
               description = "Retrieve only active subscriptions for a specific user")
    public ResponseEntity<Page<UserSubscription>> getUserActiveSubscriptions(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserSubscription> subscriptions = userSubscriptionService.getUserActiveSubscriptions(userId, pageable);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/current")
    @Operation(summary = "Get current active subscription for a user", 
               description = "Retrieve the current active subscription for a user (if exists)")
    public ResponseEntity<?> getCurrentActiveSubscription(@PathVariable Integer userId) {
        try {
            Optional<UserSubscription> subscription = userSubscriptionService.getCurrentActiveSubscription(userId);
            if (subscription.isPresent()) {
                return ResponseEntity.ok(userSubscriptionService.getSubscriptionResponse(subscription.get().getId()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No active subscription found for user with id: " + userId);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving current subscription: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a subscription", 
               description = "Cancel an active subscription for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<?> cancelSubscription(@PathVariable Integer id) {
        try {
            userSubscriptionService.cancelSubscription(id);
            return ResponseEntity.ok("Subscription cancelled successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cancelling subscription: " + e.getMessage());
        }
    }

    @PostMapping("/change")
    @Operation(summary = "Change user subscription", 
               description = "Change/upgrade/downgrade a user's subscription to a new plan. Duration defaults to 1 month. Deactivates current subscription and creates a new one.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or subscription inactive"),
            @ApiResponse(responseCode = "404", description = "User or subscription not found")
    })
    public ResponseEntity<?> changeSubscription(@RequestBody com.v2r.v2rbackend.dto.request.ChangeSubscriptionRequest request) {
        try {
            logger.info("Processing subscription change request for userId: {}, subscriptionId: {}", 
                       request.getUserId(), request.getSubscriptionId());
            UserSubscriptionResponse response = userSubscriptionService.changeUserSubscription(request);
            logger.info("Subscription changed successfully for userId: {}", request.getUserId());
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found during subscription change: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument during subscription change: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during subscription change", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Backend error: Unable to process subscription change. Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @PostMapping("/check-expired")
    @Operation(summary = "Check and update expired subscriptions", 
               description = "Check for expired subscriptions and update their status (Admin only)")
    public ResponseEntity<?> checkExpiredSubscriptions() {
        try {
            userSubscriptionService.checkAndUpdateExpiredSubscriptions();
            return ResponseEntity.ok("Expired subscriptions updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking expired subscriptions: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/model-limit")
    @Operation(summary = "Get user's effective model limit", 
               description = "Get the effective model limit for a user (free tier if no subscription, or subscription limit)")
    public ResponseEntity<?> getUserModelLimit(@PathVariable Integer userId) {
        try {
            int modelLimit = userSubscriptionService.getEffectiveModelLimit(userId);
            return ResponseEntity.ok(new ModelLimitResponse(userId, modelLimit));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting model limit: " + e.getMessage());
        }
    }

    // Inner class for model limit response
    static class ModelLimitResponse {
        private Integer userId;
        private int modelLimit;
        private String description;

        public ModelLimitResponse(Integer userId, int modelLimit) {
            this.userId = userId;
            this.modelLimit = modelLimit;
            if (modelLimit == -1) {
                this.description = "Unlimited models";
            } else {
                this.description = modelLimit + " models";
            }
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public int getModelLimit() {
            return modelLimit;
        }

        public void setModelLimit(int modelLimit) {
            this.modelLimit = modelLimit;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
