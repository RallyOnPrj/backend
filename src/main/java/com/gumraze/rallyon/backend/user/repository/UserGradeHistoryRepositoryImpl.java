package com.gumraze.rallyon.backend.user.repository;

import com.gumraze.rallyon.backend.user.entity.UserGradeHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserGradeHistoryRepositoryImpl implements UserGradeHistoryRepository {
    private final JpaUserGradeHistoryRepository jpaUserGradeHistoryRepository;

    @Override
    public void save(UserGradeHistory userGradeHistory) {
        jpaUserGradeHistoryRepository.save(userGradeHistory);
    }
}
