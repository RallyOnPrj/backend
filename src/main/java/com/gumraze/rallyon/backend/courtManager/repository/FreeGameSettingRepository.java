package com.gumraze.rallyon.backend.courtManager.repository;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FreeGameSettingRepository extends JpaRepository<FreeGameSetting, Long> {
    Optional<FreeGameSetting> findByFreeGameId(UUID freeGameId);
}
