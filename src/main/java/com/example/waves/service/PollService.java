package com.example.waves.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.waves.dto.CreatePollRequest;
import com.example.waves.dto.PollOptionResponse;
import com.example.waves.dto.PollResponse;
import com.example.waves.dto.VoteRequest;
import com.example.waves.entity.Poll;
import com.example.waves.entity.PollOption;
import com.example.waves.entity.User;
import com.example.waves.entity.Vote;
import com.example.waves.repository.PollOptionRepository;
import com.example.waves.repository.PollRepository;
import com.example.waves.repository.UserRepository;
import com.example.waves.repository.VoteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollService {
    
    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public PollResponse createPoll(CreatePollRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getUserType() != User.UserType.ADMIN) {
            throw new RuntimeException("Only admin users can create polls");
        }
        
        // Create poll
        Poll poll = new Poll();
        poll.setQuestion(request.getQuestion());
        poll.setHidden(request.isHidden());
        poll.setEndsAt(request.getEndsAt());
        poll.setCreatedBy(user);
        
        Poll savedPoll = pollRepository.save(poll);
        
        // Create poll options
        List<PollOption> options = request.getOptions().stream()
                .map(optionText -> {
                    PollOption option = new PollOption();
                    option.setOptionText(optionText);
                    option.setPoll(savedPoll);
                    return pollOptionRepository.save(option);
                })
                .collect(Collectors.toList());
        
        savedPoll.setOptions(options);
        
        log.info("Poll created successfully by admin {}: {}", userEmail, request.getQuestion());
        
        return buildPollResponse(savedPoll, userEmail, false, null);
    }
    
    public List<PollResponse> getAllPolls() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        List<Poll> polls = pollRepository.findByIsHiddenFalseOrderByCreatedAtDesc();
        
        return polls.stream()
                .map(poll -> {
                    boolean hasVoted = pollRepository.hasUserVotedOnPoll(poll.getId(), userEmail);
                    Long userVoteOptionId = pollRepository.getUserVoteOptionId(poll.getId(), userEmail).orElse(null);
                    return buildPollResponse(poll, userEmail, hasVoted, userVoteOptionId);
                })
                .collect(Collectors.toList());
    }
    
    public PollResponse getPollById(Long pollId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        if (poll.isHidden()) {
            throw new RuntimeException("Poll is hidden");
        }
        
        boolean hasVoted = pollRepository.hasUserVotedOnPoll(pollId, userEmail);
        Long userVoteOptionId = pollRepository.getUserVoteOptionId(pollId, userEmail).orElse(null);
        
        return buildPollResponse(poll, userEmail, hasVoted, userVoteOptionId);
    }
    
    @Transactional
    public PollResponse voteOnPoll(Long pollId, VoteRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        if (poll.isHidden()) {
            throw new RuntimeException("Poll is hidden");
        }
        
        if (!poll.isActive()) {
            throw new RuntimeException("Poll has ended");
        }
        
        PollOption option = pollOptionRepository.findById(request.getPollOptionId())
                .orElseThrow(() -> new RuntimeException("Poll option not found"));
        
        if (!option.getPoll().getId().equals(pollId)) {
            throw new RuntimeException("Invalid poll option for this poll");
        }
        
        // Remove existing vote if user has already voted
        voteRepository.deleteByPollIdAndUserId(pollId, user.getId());
        
        // Create new vote
        Vote vote = new Vote();
        vote.setUser(user);
        vote.setPoll(poll);
        vote.setPollOption(option);
        voteRepository.save(vote);
        
        log.info("User {} voted on poll {} for option: {}", userEmail, pollId, option.getOptionText());
        
        // Return updated poll with vote ratios
        return getPollById(pollId);
    }
    
    // Admin-only method to get all polls (including hidden ones)
    public List<PollResponse> getAllPollsForAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user is ADMIN
        if (user.getUserType() != User.UserType.ADMIN) {
            throw new RuntimeException("Only admin users can view all polls");
        }
        
        List<Poll> polls = pollRepository.findAllByOrderByCreatedAtDesc();
        
        return polls.stream()
                .map(poll -> {
                    boolean hasVoted = pollRepository.hasUserVotedOnPoll(poll.getId(), userEmail);
                    Long userVoteOptionId = pollRepository.getUserVoteOptionId(poll.getId(), userEmail).orElse(null);
                    return buildPollResponse(poll, userEmail, hasVoted, userVoteOptionId);
                })
                .collect(Collectors.toList());
    }
    
    // Admin-only method to get polls created by admin
    public List<PollResponse> getPollsCreatedByAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user is ADMIN
        if (user.getUserType() != User.UserType.ADMIN) {
            throw new RuntimeException("Only admin users can view created polls");
        }
        
        List<Poll> polls = pollRepository.findByCreatedByEmailOrderByCreatedAtDesc(userEmail);
        
        return polls.stream()
                .map(poll -> {
                    boolean hasVoted = pollRepository.hasUserVotedOnPoll(poll.getId(), userEmail);
                    Long userVoteOptionId = pollRepository.getUserVoteOptionId(poll.getId(), userEmail).orElse(null);
                    return buildPollResponse(poll, userEmail, hasVoted, userVoteOptionId);
                })
                .collect(Collectors.toList());
    }
    
    public List<PollResponse> getPollsVotedByUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        List<Poll> polls = pollRepository.findPollsVotedByUser(userEmail);
        
        return polls.stream()
                .map(poll -> {
                    boolean hasVoted = pollRepository.hasUserVotedOnPoll(poll.getId(), userEmail);
                    Long userVoteOptionId = pollRepository.getUserVoteOptionId(poll.getId(), userEmail).orElse(null);
                    return buildPollResponse(poll, userEmail, hasVoted, userVoteOptionId);
                })
                .collect(Collectors.toList());
    }
    
    private PollResponse buildPollResponse(Poll poll, String userEmail, boolean hasVoted, Long userVoteOptionId) {
        int totalVotes = voteRepository.countVotesByPollId(poll.getId());
        
        List<PollOptionResponse> optionResponses = poll.getOptions().stream()
                .map(option -> {
                    int voteCount = voteRepository.countVotesByOptionId(option.getId());
                    double percentage = totalVotes > 0 ? (double) voteCount / totalVotes * 100 : 0;
                    boolean isUserVote = userVoteOptionId != null && userVoteOptionId.equals(option.getId());
                    
                    return PollOptionResponse.builder()
                            .id(option.getId())
                            .optionText(option.getOptionText())
                            .voteCount(voteCount)
                            .votePercentage(hasVoted ? percentage : 0) // Only show percentage if user voted
                            .isUserVote(isUserVote)
                            .build();
                })
                .collect(Collectors.toList());
        
        return PollResponse.builder()
                .id(poll.getId())
                .question(poll.getQuestion())
                .isHidden(poll.isHidden())
                .createdAt(poll.getCreatedAt())
                .updatedAt(poll.getUpdatedAt())
                .endsAt(poll.getEndsAt())
                .createdByUsername(poll.getCreatedBy().getProfile().getUsername())
                .createdByUserType(poll.getCreatedBy().getUserType().name())
                .options(optionResponses)
                .totalVotes(totalVotes)
                .isActive(poll.isActive())
                .hasUserVoted(hasVoted)
                .userVoteOptionId(userVoteOptionId)
                .build();
    }
} 