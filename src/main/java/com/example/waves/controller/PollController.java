package com.example.waves.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.waves.dto.CreatePollRequest;
import com.example.waves.dto.PollResponse;
import com.example.waves.dto.VoteRequest;
import com.example.waves.service.PollService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
@Slf4j
public class PollController {
    
    private final PollService pollService;
    
    // Admin-only endpoint to create polls
    @PostMapping
    public ResponseEntity<PollResponse> createPoll(@Valid @RequestBody CreatePollRequest request) {
        try {
            PollResponse response = pollService.createPoll(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create poll: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Public endpoint for all users to view polls
    @GetMapping
    public ResponseEntity<List<PollResponse>> getAllPolls() {
        try {
            List<PollResponse> polls = pollService.getAllPolls();
            return ResponseEntity.ok(polls);
        } catch (Exception e) {
            log.error("Failed to get polls: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Public endpoint for all users to view specific poll
    @GetMapping("/{pollId}")
    public ResponseEntity<PollResponse> getPollById(@PathVariable Long pollId) {
        try {
            PollResponse poll = pollService.getPollById(pollId);
            return ResponseEntity.ok(poll);
        } catch (Exception e) {
            log.error("Failed to get poll {}: {}", pollId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Public endpoint for all users to vote
    @PostMapping("/{pollId}/vote")
    public ResponseEntity<PollResponse> voteOnPoll(@PathVariable Long pollId, @Valid @RequestBody VoteRequest request) {
        try {
            PollResponse response = pollService.voteOnPoll(pollId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to vote on poll {}: {}", pollId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Admin-only endpoint to view all polls (including hidden ones)
    @GetMapping("/admin/all")
    public ResponseEntity<List<PollResponse>> getAllPollsForAdmin() {
        try {
            List<PollResponse> polls = pollService.getAllPollsForAdmin();
            return ResponseEntity.ok(polls);
        } catch (Exception e) {
            log.error("Failed to get all polls for admin: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Admin-only endpoint to view polls created by admin
    @GetMapping("/admin/created")
    public ResponseEntity<List<PollResponse>> getPollsCreatedByAdmin() {
        try {
            List<PollResponse> polls = pollService.getPollsCreatedByAdmin();
            return ResponseEntity.ok(polls);
        } catch (Exception e) {
            log.error("Failed to get polls created by admin: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Public endpoint for users to view polls they voted on
    @GetMapping("/my-votes")
    public ResponseEntity<List<PollResponse>> getPollsVotedByUser() {
        try {
            List<PollResponse> polls = pollService.getPollsVotedByUser();
            return ResponseEntity.ok(polls);
        } catch (Exception e) {
            log.error("Failed to get polls voted by user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}