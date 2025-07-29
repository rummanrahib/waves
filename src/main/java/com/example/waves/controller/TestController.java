package com.example.waves.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint - anyone can access it!";
    }
    
    @GetMapping("/protected")
    public String protectedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "This is a protected endpoint - Hello, " + authentication.getName() + "!";
    }
    
    @GetMapping("/admin")
    public String adminEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "This is an admin endpoint - Hello, " + authentication.getName() + "!";
    }
} 