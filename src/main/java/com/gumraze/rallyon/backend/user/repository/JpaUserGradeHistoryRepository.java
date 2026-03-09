package com.gumraze.rallyon.backend.user.repository;

import com.gumraze.rallyon.backend.user.entity.UserGradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserGradeHistoryRepository extends JpaRepository<UserGradeHistory, Long> {
}
