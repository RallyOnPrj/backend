package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class LoadAccountPersistenceAdapterTest {

    private AccountRepository accountRepository;
    private LoadAccountPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        adapter = new LoadAccountPersistenceAdapter(accountRepository);
    }

    @Test
    @DisplayName("accountId로 계정 존재 여부를 조회한다")
    void existsBy_returnsBoolean() {
        UUID accountId = UUID.randomUUID();
        given(accountRepository.existsById(accountId)).willReturn(true);

        boolean result = adapter.existsById(accountId);

        assertThat(result).isTrue();
    }
}
