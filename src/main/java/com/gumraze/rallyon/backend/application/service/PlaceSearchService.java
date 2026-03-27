package com.gumraze.rallyon.backend.application.service;

import com.gumraze.rallyon.backend.application.port.in.SearchPlacesUseCase;
import com.gumraze.rallyon.backend.application.port.out.PlaceSearchPort;
import com.gumraze.rallyon.backend.domain.PlaceSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "naver.search.local", name = "enabled", havingValue = "true")
public class PlaceSearchService implements SearchPlacesUseCase {

    private static final int MAX_SEARCH_RESULTS = 5;
    private final PlaceSearchPort placeSearchPort;

    @Override
    public List<PlaceSearchResult> search(String query) {
        String normalized = query == null ? "" : query.trim();

        if (normalized.isEmpty() || normalized.length() < 2) {
            return List.of();
        }

        return placeSearchPort.search(normalized, MAX_SEARCH_RESULTS);
    }
}
