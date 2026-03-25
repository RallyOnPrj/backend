package com.gumraze.rallyon.backend.user.adapter.out.persistence;

import com.gumraze.rallyon.backend.user.application.port.out.SaveUserGradeHistoryPort;
import com.gumraze.rallyon.backend.user.entity.UserGradeHistory;
import com.gumraze.rallyon.backend.user.adapter.out.persistence.repository.UserGradeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserGradeHistoryPersistenceAdapter implements SaveUserGradeHistoryPort {

    private final UserGradeHistoryRepository userGradeHistoryRepository;

    @Override
    public void save(UserGradeHistory userGradeHistory) {
        userGradeHistoryRepository.save(userGradeHistory);
    }
}
