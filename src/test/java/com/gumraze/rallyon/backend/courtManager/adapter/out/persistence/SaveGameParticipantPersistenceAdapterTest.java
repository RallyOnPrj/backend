package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.repository.GameParticipantRepository;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class SaveGameParticipantPersistenceAdapterTest {

    private GameParticipantRepository gameParticipantRepository;
    private SaveGameParticipantPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        gameParticipantRepository = mock(GameParticipantRepository.class);
        adapter = new SaveGameParticipantPersistenceAdapter(gameParticipantRepository);
    }

    @Test
    @DisplayName("참가자를 저장하고 clientId 기준 매핑을 반환한다.")
    void saveAll_returnsParticipantsByClientId() {
        // given
        UUID gameId = UUID.randomUUID();

        FreeGame freeGame = FreeGame.builder()
                .id(gameId)
                .title("자유게임")
                .build();

        List<CreateFreeGameCommand.Participant> participants = List.of(
                new CreateFreeGameCommand.Participant("p1", null, "서승재", Gender.MALE, Grade.SS, 20),
                new CreateFreeGameCommand.Participant("p2", null, "김원호", Gender.MALE, Grade.SS, 20)
        );

        // AtomicInteger: 증가하는 숫자 카운터로 호출 시마다 값이 증가함. 이는 람다 안에서 값을 안전하게 증가시킬 수 있기 때문에 사용함.
        AtomicInteger sequence = new AtomicInteger(1);


        given(gameParticipantRepository.save(any(GameParticipant.class)))       // save 호출마다
                .willAnswer(invocation -> {
                    GameParticipant participant = invocation.getArgument(0);  // save에 들어온 participant 꺼냄
                    UUID participantId = UUID.fromString(                       // 예측 가능한 UUID 설계
                            String.format("018f1a1e-2b2f-7c11-9a55-%012d", sequence.getAndIncrement())
                    );
                    // participant를 reflection을 이용하여 private field인 'id'에 값을 채워줌
                    // reflection: 런터임 구조를 조작하는 방법
                    ReflectionTestUtils.setField(
                            participant, "id", participantId
                    );
                    return participant;
                });

        // when
        Map<String, GameParticipant> result = adapter.saveAll(freeGame, participants);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("p1", "p2");
        assertThat(result.get("p1").getOriginalName()).isEqualTo("서승재");
        assertThat(result.get("p2").getOriginalName()).isEqualTo("김원호");
    }
}
