package com.v2r.v2rbackend.controller;

import com.v2r.v2rbackend.dto.request.CreateUserRequest;
import com.v2r.v2rbackend.dto.response.UserResponse;
import com.v2r.v2rbackend.entity.User;
import com.v2r.v2rbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operations related to users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all users with pagination", 
               description = "Get all users with their roles. Use page and size query parameters for pagination.")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.findAllWithRoles(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user with email, password, and role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(request);
            
            // Convert to response DTO (without password)
            UserResponse response = new UserResponse();
            response.setUserID(user.getUserID());
            response.setEmail(user.getEmail());
            response.setFullName(user.getFullName());
            response.setLoyaltyPoints(user.getLoyaltyPoints());
            response.setVerified(user.isVerified());
            response.setAddress(user.getAddress());
            response.setPhone(user.getPhone());
            response.setAvatar(user.getAvatar());
            response.setStatus(user.isStatus());
            
            if (user.getRole() != null) {
                UserResponse.RoleResponse roleResponse = new UserResponse.RoleResponse();
                roleResponse.setRoleID(user.getRole().getRoleID());
                roleResponse.setRoleName(user.getRole().getRoleName());
                response.setRole(roleResponse);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User user) {
        user.setUserID(id);
        return ResponseEntity.ok(userService.update(user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    @GetMapping("/status/{verified}")
    @Operation(summary = "Get users by verification status with pagination")
    public ResponseEntity<Page<User>> getUsersByVerificationStatus(
            @PathVariable boolean verified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.findByIsVerified(verified, pageable));
    }

    @PatchMapping("/{id}/verify")
    @Operation(summary = "Verify a user")
    public ResponseEntity<User> verifyUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.verifyUser(id));
    }

    @PatchMapping("/{id}/loyalty-points")
    @Operation(summary = "Update loyalty points")
    public ResponseEntity<User> updateLoyaltyPoints(@PathVariable Integer id, @RequestBody Integer points) {
        return ResponseEntity.ok(userService.updateLoyaltyPoints(id, points));
    }
}