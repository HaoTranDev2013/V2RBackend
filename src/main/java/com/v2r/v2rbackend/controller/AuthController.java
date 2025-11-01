package com.v2r.v2rbackend.controller;

import com.v2r.v2rbackend.dto.request.LoginRequest;
import com.v2r.v2rbackend.dto.request.RegisterRequest;
import com.v2r.v2rbackend.dto.request.VerifyOtpRequest;
import com.v2r.v2rbackend.dto.response.AuthResponse;
import com.v2r.v2rbackend.entity.User;
import com.v2r.v2rbackend.repository.RoleRepository;
import com.v2r.v2rbackend.repository.UserRepository;
import com.v2r.v2rbackend.security.JwtUtil;
import com.v2r.v2rbackend.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints for login and registration")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OtpService otpService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // Check if user is verified
            if (!user.isVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account not verified. Please verify your account with the OTP sent to your email.");
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole() != null ? user.getRole().getRoleName() : "USER");
            claims.put("userId", user.getUserID());

            String token = jwtUtil.generateToken(userDetails.getUsername(), claims);

            String roleName = user.getRole() != null ? user.getRole().getRoleName() : "USER";
            AuthResponse response = new AuthResponse(token, user.getEmail(), roleName);

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
            }

            // Get role or use default
//            Role role;
//            if (registerRequest.getRoleId() != null) {
//                role = roleRepository.findById(registerRequest.getRoleId())
//                        .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + registerRequest.getRoleId()));
//            } else {
//                // Default role (you might want to create a default "USER" role)
//                role = roleRepository.findByRoleName("USER")
//                        .orElse(null);
//            }

            // Create new user
            User user = new User();
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setFullName(registerRequest.getFullName());
            user.setRole(roleRepository.findByRoleName("USER").orElse(null));
            user.setStatus(true);
            user.setVerified(false);
            user.setNumberOfModel(3) ; // Default number of models
            user.setLoyaltyPoints(0);

            userRepository.save(user);

            // Generate and send OTP
            try {
                otpService.generateAndSendOtp(registerRequest.getEmail());
            } catch (Exception e) {
                // Log the error but don't fail registration
                System.err.println("Failed to send OTP: " + e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully. Please check your email for OTP verification code.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify user account with OTP code sent to email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest verifyOtpRequest) {
        try {
            // Verify OTP
            boolean isValid = otpService.verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtpCode());
            
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP code");
            }

            // Update user verification status
            User user = userRepository.findByEmail(verifyOtpRequest.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            
            user.setVerified(true);
            userRepository.save(user);

            return ResponseEntity.ok("Account verified successfully. You can now login.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP", description = "Resend OTP verification code to email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "User already verified")
    })
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            
            if (user.isVerified()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is already verified");
            }

            otpService.generateAndSendOtp(email);
            return ResponseEntity.ok("OTP sent successfully. Please check your email.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP: " + e.getMessage());
        }
    }
}
