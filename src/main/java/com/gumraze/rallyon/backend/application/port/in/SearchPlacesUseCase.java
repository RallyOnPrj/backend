package com.gumraze.rallyon.backend.application.port.in;

import com.gumraze.rallyon.backend.domain.PlaceSearchResult;

import java.util.List;

public interface SearchPlacesUseCase {
    List<PlaceSearchResult> search(String query);
}
