package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameSettingRepository;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SaveFreeGameSettingPersistenceAdapterTest {

    private FreeGameSettingRepository freeGameSettingRepository;
    private SaveFreeGameSettingPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        freeGameSettingRepository = mock(FreeGameSettingRepository.class);
        adapter = new SaveFreeGameSettingPersistenceAdapter(freeGameSettingRepository);
    }

    @Test
    @DisplayName("자유게임 setting을 저장한다")
    void save_persistsFreeGameSetting() {
        FreeGame freeGame = FreeGame.create(
                "자유게임",
                UUID.randomUUID(),
                GradeType.NATIONAL,
                MatchRecordMode.STATUS_ONLY,
                null,
                null
        );
        ReflectionTestUtils.setField(freeGame, "id", UUID.randomUUID());
        given(freeGameSettingRepository.save(org.mockito.ArgumentMatchers.any(FreeGameSetting.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        adapter.save(freeGame, 2, 3);

        // then
        ArgumentCaptor<FreeGameSetting> captor = ArgumentCaptor.forClass(FreeGameSetting.class);
        verify(freeGameSettingRepository).save(captor.capture());
        FreeGameSetting savedSetting = captor.getValue();
        assertThat(savedSetting.getFreeGame()).isEqualTo(freeGame);
        assertThat(savedSetting.getCourtCount()).isEqualTo(2);
        assertThat(savedSetting.getRoundCount()).isEqualTo(3);
    }
}
