package com.gumraze.rallyon.backend.application.service;

import com.gumraze.rallyon.backend.application.port.in.SearchPlacesUseCase;
import com.gumraze.rallyon.backend.application.port.out.PlaceSearchPort;
import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import com.gumraze.rallyon.backend.domain.PlaceSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceSearchService implements SearchPlacesUseCase {

    private static final int MAX_SEARCH_RESULTS = 5;
    private final ObjectProvider<PlaceSearchPort> placeSearchPortProvider;

    @Override
    public List<PlaceSearchResult> search(String query) {
        String normalized = query == null ? "" : query.trim();

        if (normalized.isEmpty() || normalized.length() < 2) {
            return List.of();
        }

        PlaceSearchPort placeSearchPort = placeSearchPortProvider.getIfAvailable();
        if (placeSearchPort == null) {
            throw new ServiceUnavailableException("장소 검색 서비스를 현재 사용할 수 없습니다.");
        }

        return placeSearchPort.search(normalized, MAX_SEARCH_RESULTS);
    }
}
