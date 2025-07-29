package com.example.waves.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.waves.dto.AuthResponse;
import com.example.waves.dto.ChangePasswordRequest;
import com.example.waves.dto.LoginRequest;
import com.example.waves.dto.RegisterRequest;
import com.example.waves.entity.User;
import com.example.waves.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request);
            
            String referralInfo = "";
            if (user.isReferred() && user.getReferredBy() != null) {
                Optional<User> referrer = userService.getReferrer(user.getReferredBy());
                if (referrer.isPresent()) {
                    User referrerUser = referrer.get();
                    referralInfo = " (Referred by: " + referrerUser.getProfile().getUsername() + " - " + referrerUser.getEmail() + ")";
                }
            }
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(true)
                    .email(user.getEmail())
                    .username(user.getProfile().getUsername())
                    .userType(user.getUserType().name())
                    .message("User registered successfully" + referralInfo)
                    .build());
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = (User) authentication.getPrincipal();
            userService.updateLastLogin(user.getEmail());
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(true)
                    .email(user.getEmail())
                    .username(user.getProfile().getUsername())
                    .userType(user.getUserType().name())
                    .message("Login successful")
                    .build());
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                    .success(false)
                    .message("Invalid email or password")
                    .build());
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            userService.changePassword(userEmail, request);
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(true)
                    .message("Password changed successfully")
                    .build());
        } catch (Exception e) {
            log.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }
    
    @GetMapping("/validate-referral/{referralCode}")
    public ResponseEntity<AuthResponse> validateReferralCode(@PathVariable String referralCode) {
        boolean isValid = userService.isValidReferralCode(referralCode);
        
        return ResponseEntity.ok(AuthResponse.builder()
                .success(isValid)
                .message(isValid ? "Valid referral code" : "Invalid referral code")
                .build());
    }
    
    @GetMapping("/my-referral-code")
    public ResponseEntity<AuthResponse> getMyReferralCode() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            User user = userService.findByEmail(userEmail).orElseThrow();
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(true)
                    .message("Your referral code: " + user.getReferralCode())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                    .success(false)
                    .message("Failed to get referral code")
                    .build());
        }
    }
} 