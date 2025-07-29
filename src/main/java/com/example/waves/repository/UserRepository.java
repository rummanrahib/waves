package com.example.waves.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.waves.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByReferralCode(String referralCode);
    
    boolean existsByEmail(String email);
    
    boolean existsByReferralCode(String referralCode);
    
    @Query("SELECT u FROM User u WHERE u.userType = :userType")
    java.util.List<User> findByUserType(@Param("userType") User.UserType userType);
    
    @Query("SELECT u FROM User u WHERE u.referredBy = :referralCode")
    java.util.List<User> findByReferredBy(@Param("referralCode") String referralCode);
} 