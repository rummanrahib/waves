package com.example.waves.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.waves.entity.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    
    // Check if user has already voted on a poll
    boolean existsByPollIdAndUserId(Long pollId, Long userId);
    
    // Find user's vote for a specific poll
    Optional<Vote> findByPollIdAndUserId(Long pollId, Long userId);
    
    // Delete user's vote for a specific poll (for changing vote)
    void deleteByPollIdAndUserId(Long pollId, Long userId);
    
    // Count votes for a specific poll option
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.pollOption.id = :optionId")
    int countVotesByOptionId(@Param("optionId") Long optionId);
    
    // Count total votes for a poll
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.poll.id = :pollId")
    int countVotesByPollId(@Param("pollId") Long pollId);
} 