package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class LoadUserPersistenceAdapterTest {

    private UserRepository userRepository;
    private LoadUserPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        adapter = new LoadUserPersistenceAdapter(userRepository);
    }

    @Test
    @DisplayName("userId로 사용자를 조회한다.")
    void loadBy_returnsUser() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();

        given(userRepository.findById(userId)).willReturn(java.util.Optional.of(user));

        // when
        Optional<User> result = adapter.loadById(userId);

        // then
        assertThat(result).contains(user);
        assertThat(result.get().getId()).isEqualTo(userId);
    }
}
