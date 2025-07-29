package com.example.waves.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.waves.entity.User;
import com.example.waves.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor
@Slf4j
public class ReferralController {
    
    private final UserService userService;
    
    @GetMapping("/my-referrals")
    public ResponseEntity<Map<String, Object>> getMyReferrals() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            User user = userService.findByEmail(userEmail).orElseThrow();
            List<User> referredUsers = userService.getUsersReferredBy(user.getReferralCode());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("referralCode", user.getReferralCode());
            response.put("referredCount", referredUsers.size());
            response.put("referredUsers", referredUsers.stream()
                    .map(u -> Map.of(
                            "email", u.getEmail(),
                            "username", u.getProfile().getUsername(),
                            "fullName", u.getProfile().getFullName(),
                            "registeredAt", u.getCreatedAt()
                    ))
                    .toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get referral data");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getReferralStats() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            User user = userService.findByEmail(userEmail).orElseThrow();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("referralCode", user.getReferralCode());
            response.put("isReferred", user.isReferred());
            response.put("referredCount", userService.getUsersReferredBy(user.getReferralCode()).size());
            
            // Get referrer information if user was referred
            if (user.isReferred() && user.getReferredBy() != null) {
                Optional<User> referrer = userService.getReferrer(user.getReferredBy());
                if (referrer.isPresent()) {
                    User referrerUser = referrer.get();
                    Map<String, Object> referrerInfo = new HashMap<>();
                    referrerInfo.put("email", referrerUser.getEmail());
                    referrerInfo.put("username", referrerUser.getProfile().getUsername());
                    referrerInfo.put("fullName", referrerUser.getProfile().getFullName());
                    referrerInfo.put("referralCode", referrerUser.getReferralCode());
                    response.put("referredBy", referrerInfo);
                }
            } else {
                response.put("referredBy", null);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get referral stats");
            return ResponseEntity.badRequest().body(response);
        }
    }
} 