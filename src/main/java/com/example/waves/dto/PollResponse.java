package com.example.waves.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollResponse {
    private Long id;
    private String question;
    private boolean isHidden;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime endsAt;
    private String createdByUsername;
    private String createdByUserType; // ADMIN, STAFF, etc.
    private List<PollOptionResponse> options;
    private int totalVotes;
    private boolean isActive;
    private boolean hasUserVoted;
    private Long userVoteOptionId; // Which option the user voted for (if voted)
}