package com.example.waves.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.waves.dto.ChangePasswordRequest;
import com.example.waves.dto.RegisterRequest;
import com.example.waves.entity.User;
import com.example.waves.entity.UserProfile;
import com.example.waves.repository.UserProfileRepository;
import com.example.waves.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
    
    @Transactional
    public User registerUser(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        // Check if username already exists
        if (userProfileRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        
        // Validate referral code if provided
        User referrer = null;
        if (request.getReferredBy() != null && !request.getReferredBy().isEmpty()) {
            Optional<User> referrerOpt = userRepository.findByReferralCode(request.getReferredBy());
            if (referrerOpt.isEmpty()) {
                throw new RuntimeException("Invalid referral code");
            }
            referrer = referrerOpt.get();
        }
        
        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(true); // For now, auto-verify email
        
        // Handle referral - only set if actually referred
        if (referrer != null) {
            user.setReferredBy(referrer.getReferralCode());
            user.setReferred(true);
        } else {
            user.setReferred(false);
            user.setReferredBy(null);
        }
        
        // Generate unique referral code
        user.setReferralCode(generateUniqueReferralCode());
        
        User savedUser = userRepository.save(user);
        
        // Create user profile
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setUsername(request.getUsername());
        profile.setFullName(request.getFullName());
        
        userProfileRepository.save(profile);
        
        log.info("User registered successfully: {} (Referred by: {})", 
                request.getEmail(), 
                referrer != null ? referrer.getEmail() : "No referral");
        return savedUser;
    }
    
    private String generateUniqueReferralCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            code = sb.toString();
        } while (userRepository.existsByReferralCode(code)); // Until unique
        
        return code;
    }
    
    @Transactional
    public boolean changePassword(String userEmail, ChangePasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", userEmail);
        return true;
    }
    
    public void updateLastLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByReferralCode(String referralCode) {
        return userRepository.findByReferralCode(referralCode);
    }
    
    public boolean isValidReferralCode(String referralCode) {
        return userRepository.existsByReferralCode(referralCode);
    }
    
    public java.util.List<User> getUsersReferredBy(String referralCode) {
        return userRepository.findByReferredBy(referralCode);
    }
    
    public Optional<User> getReferrer(String referralCode) {
        return userRepository.findByReferralCode(referralCode);
    }
} 