package com.gumraze.rallyon.backend.courtManager.repository;

import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameParticipantRepository extends JpaRepository<GameParticipant, UUID> {
    List<GameParticipant> findByFreeGameId(UUID freeGameId);
}
