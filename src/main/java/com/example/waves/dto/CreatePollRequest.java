package com.example.waves.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePollRequest {
    
    @NotBlank(message = "Question is required")
    @Size(max = 500, message = "Question must be less than 500 characters")
    private String question;
    
    @NotEmpty(message = "At least two options are required")
    @Size(min = 2, max = 3, message = "Poll must have between 2 and 3 options")
    private List<String> options;
    
    private LocalDateTime endsAt; // Optional end date
    
    private boolean isHidden = false;
} 