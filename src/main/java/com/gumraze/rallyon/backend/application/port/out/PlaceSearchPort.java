package com.gumraze.rallyon.backend.application.port.out;

import com.gumraze.rallyon.backend.domain.PlaceSearchResult;

import java.util.List;

public interface PlaceSearchPort {
    List<PlaceSearchResult> search(String query, int limit);
}
