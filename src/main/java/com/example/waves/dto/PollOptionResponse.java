package com.example.waves.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollOptionResponse {
    private Long id;
    private String optionText;
    private int voteCount;
    private double votePercentage; // Only shown if user has voted
    private boolean isUserVote; // Whether this is the option the user voted for
}
