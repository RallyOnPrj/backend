package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGameSettingPort;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveFreeGameSettingPersistenceAdapter implements SaveFreeGameSettingPort {

    private final FreeGameSettingRepository freeGameSettingRepository;

    @Override
    public void save(FreeGame freeGame, Integer courtCount, Integer roundCount) {
        freeGameSettingRepository.save(
                FreeGameSetting.builder()
                        .freeGame(freeGame)
                        .courtCount(courtCount)
                        .roundCount(roundCount)
                        .build()
        );
    }
}
