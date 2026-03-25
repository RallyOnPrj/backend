package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class LoadUserPersistenceAdapterTest {

    private IdentityAccountRepository identityAccountRepository;
    private LoadUserPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        identityAccountRepository = mock(IdentityAccountRepository.class);
        adapter = new LoadUserPersistenceAdapter(identityAccountRepository);
    }

    @Test
    @DisplayName("identityAccountId로 계정 존재 여부를 조회한다")
    void existsBy_returnsBoolean() {
        UUID identityAccountId = UUID.randomUUID();
        given(identityAccountRepository.existsById(identityAccountId)).willReturn(true);

        boolean result = adapter.existsById(identityAccountId);

        assertThat(result).isTrue();
    }
}
