package com.example.waves.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to Waves - Heal Your Soul!";
    }

    @GetMapping("/health")
    public String health() {
        return "Application is running!";
    }
} 