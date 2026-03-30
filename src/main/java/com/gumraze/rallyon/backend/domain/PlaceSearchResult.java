package com.gumraze.rallyon.backend.domain;

public record PlaceSearchResult(
        String name,
        String roadAddress,
        String address,
        String category,
        String longitude,
        String latitude,
        String link
) {

}
