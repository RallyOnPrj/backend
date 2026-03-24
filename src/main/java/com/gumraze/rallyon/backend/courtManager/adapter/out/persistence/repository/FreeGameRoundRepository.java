package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FreeGameRoundRepository extends JpaRepository<FreeGameRound, UUID> {
    List<FreeGameRound> findByFreeGameIdOrderByRoundNumber(UUID freeGameId);
}
