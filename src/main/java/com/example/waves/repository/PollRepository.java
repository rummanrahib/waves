package com.example.waves.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.waves.entity.Poll;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
    
    // Find all visible polls
    List<Poll> findByIsHiddenFalseOrderByCreatedAtDesc();
    
    // Find all polls (for admin)
    List<Poll> findAllByOrderByCreatedAtDesc();
    
    // Find polls by creator (admin)
    List<Poll> findByCreatedByEmailOrderByCreatedAtDesc(String email);
    
    // Find active polls (not ended)
    @Query("SELECT p FROM Poll p WHERE p.isHidden = false AND (p.endsAt IS NULL OR p.endsAt > CURRENT_TIMESTAMP) ORDER BY p.createdAt DESC")
    List<Poll> findActivePolls();
    
    // Find polls where user has voted
    @Query("SELECT DISTINCT p FROM Poll p JOIN p.votes v WHERE v.user.email = :userEmail ORDER BY p.createdAt DESC")
    List<Poll> findPollsVotedByUser(@Param("userEmail") String userEmail);
    
    // Check if user has voted on a specific poll
    @Query("SELECT COUNT(v) > 0 FROM Vote v WHERE v.poll.id = :pollId AND v.user.email = :userEmail")
    boolean hasUserVotedOnPoll(@Param("pollId") Long pollId, @Param("userEmail") String userEmail);
    
    // Get user's vote for a specific poll
    @Query("SELECT v.pollOption.id FROM Vote v WHERE v.poll.id = :pollId AND v.user.email = :userEmail")
    Optional<Long> getUserVoteOptionId(@Param("pollId") Long pollId, @Param("userEmail") String userEmail);
}