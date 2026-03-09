package com.gumraze.rallyon.backend.courtManager.repository;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreeGameMatchRepository extends JpaRepository<FreeGameMatch, Long> {
    List<FreeGameMatch> findByRoundIdInOrderByCourtNumber(List<Long> roundIds);

    void deleteByRoundId(Long id);
}

