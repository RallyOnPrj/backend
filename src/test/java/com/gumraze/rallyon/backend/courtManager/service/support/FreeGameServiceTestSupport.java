package com.gumraze.rallyon.backend.courtManager.service.support;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.repository.GameRepository;
import com.gumraze.rallyon.backend.courtManager.service.ShareCodeGenerator;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public interface FreeGameServiceTestSupport {

    GameRepository gameRepository();

    UserRepository userRepository();

    ShareCodeGenerator shareCodeGenerator();

    default User organizer(Long userId) {
        User organizer = mock(User.class);
        lenient().when(organizer.getId()).thenReturn(userId);
        return organizer;
    }

    default void stubUserExists(Long... userIds) {
        for (Long userId : userIds) {
            when(userRepository().existsById(userId)).thenReturn(true);
        }
    }

    default void stubOrganizer(Long userId, User organizer) {
        stubUserExists(userId);
        when(userRepository().findById(userId)).thenReturn(Optional.of(organizer));
    }

    default void stubShareCode(String shareCode) {
        when(shareCodeGenerator().generate()).thenReturn(shareCode);
        when(gameRepository().existsByShareCode(shareCode)).thenReturn(false);
    }

    default ArgumentCaptor<FreeGame> savedGameCaptor() {
        ArgumentCaptor<FreeGame> captor = ArgumentCaptor.forClass(FreeGame.class);
        verify(gameRepository()).save(captor.capture());
        return captor;
    }
}
