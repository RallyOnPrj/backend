package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FreeGameSettingRepository extends JpaRepository<FreeGameSetting, UUID> {
    Optional<FreeGameSetting> findByFreeGameId(UUID freeGameId);
}
