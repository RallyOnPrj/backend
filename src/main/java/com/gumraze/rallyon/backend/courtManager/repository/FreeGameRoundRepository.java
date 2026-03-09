package com.gumraze.rallyon.backend.courtManager.repository;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreeGameRoundRepository extends JpaRepository<FreeGameRound, Long> {
    List<FreeGameRound> findByFreeGameIdOrderByRoundNumber(Long freeGameId);
}
