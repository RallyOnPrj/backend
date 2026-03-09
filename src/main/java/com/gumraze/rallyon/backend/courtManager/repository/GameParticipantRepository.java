package com.gumraze.rallyon.backend.courtManager.repository;

import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameParticipantRepository extends JpaRepository<GameParticipant, Long> {
    List<GameParticipant> findByFreeGameId(Long freeGameId);
}
