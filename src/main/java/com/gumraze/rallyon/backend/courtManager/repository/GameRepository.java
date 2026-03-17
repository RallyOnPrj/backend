package com.gumraze.rallyon.backend.courtManager.repository;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<FreeGame, UUID> {

    Optional<FreeGame> findByShareCode(String shareCode);
    boolean existsByShareCode(String shareCode);

}
