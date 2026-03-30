package com.gumraze.rallyon.backend.user.adapter.out.persistence.repository;

import com.gumraze.rallyon.backend.user.entity.UserGradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaUserGradeHistoryRepository extends JpaRepository<UserGradeHistory, UUID> {
}
