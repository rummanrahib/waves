package com.example.waves.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.waves.entity.PollOption;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    
    List<PollOption> findByPollIdOrderById(Long pollId);
    
    @Query("SELECT po FROM PollOption po WHERE po.poll.id = :pollId ORDER BY po.id")
    List<PollOption> findOptionsByPollId(@Param("pollId") Long pollId);
    
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.pollOption.id = :optionId")
    int getVoteCountForOption(@Param("optionId") Long optionId);
}
