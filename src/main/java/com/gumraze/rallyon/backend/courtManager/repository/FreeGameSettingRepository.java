package com.gumraze.rallyon.backend.courtManager.repository;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FreeGameSettingRepository extends JpaRepository<FreeGameSetting, Long> {
    Optional<FreeGameSetting> findByFreeGameId(Long freeGameId);
}
