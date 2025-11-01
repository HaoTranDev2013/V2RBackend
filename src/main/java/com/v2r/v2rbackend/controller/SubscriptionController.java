package com.v2r.v2rbackend.controller;

import com.v2r.v2rbackend.dto.SubscriptionDTO;
import com.v2r.v2rbackend.entity.Subscription;
import com.v2r.v2rbackend.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscription", description = "Subscription management endpoints")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping
    @Operation(summary = "Get all subscriptions with pagination", description = "Retrieve a list of all subscriptions with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<Subscription>> getAllSubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Subscription> subscriptions = subscriptionService.findAll(pageable);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription by ID", description = "Retrieve a specific subscription by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved subscription"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<?> getSubscriptionById(@PathVariable Integer id) {
        try {
            return subscriptionService.findById(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Subscription not found with id: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving subscription: " + e.getMessage());
        }
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get subscription by name", description = "Retrieve a specific subscription by its name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved subscription"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<?> getSubscriptionByName(@PathVariable String name) {
        try {
            return subscriptionService.findByName(name)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Subscription not found with name: " + name));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving subscription: " + e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get subscriptions by status with pagination", description = "Retrieve all subscriptions with a specific status with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved subscriptions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getSubscriptionsByStatus(
            @PathVariable boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Subscription> subscriptions = subscriptionService.findByStatus(status, pageable);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving subscriptions: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "Create a new subscription", description = "Create a new subscription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subscription created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or subscription already exists")
    })
    public ResponseEntity<?> createSubscription(@RequestBody SubscriptionDTO subscriptionDTO) {
        try {
            // Convert DTO to Entity
            Subscription subscription = new Subscription();
            subscription.setName(subscriptionDTO.getName());
            subscription.setStatus(subscriptionDTO.isStatus());
            subscription.setPrice(subscriptionDTO.getPrice());
            subscription.setNumberOfModel(subscriptionDTO.getNumberOfModel());
            
            Subscription createdSubscription = subscriptionService.save(subscription);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSubscription);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating subscription: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a subscription", description = "Update an existing subscription by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription updated successfully"),
            @ApiResponse(responseCode = "404", description = "Subscription not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<?> updateSubscription(@PathVariable Integer id, @RequestBody SubscriptionDTO subscriptionDTO) {
        try {
            // Convert DTO to Entity
            Subscription subscription = new Subscription();
            subscription.setName(subscriptionDTO.getName());
            subscription.setStatus(subscriptionDTO.isStatus());
            subscription.setPrice(subscriptionDTO.getPrice());
            subscription.setNumberOfModel(subscriptionDTO.getNumberOfModel());
            
            Subscription updatedSubscription = subscriptionService.update(id, subscription);
            return ResponseEntity.ok(updatedSubscription);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating subscription: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a subscription", description = "Delete a subscription by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<?> deleteSubscription(@PathVariable Integer id) {
        try {
            subscriptionService.deleteById(id);
            return ResponseEntity.ok("Subscription deleted successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting subscription: " + e.getMessage());
        }
    }
}
