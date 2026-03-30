package com.gumraze.rallyon.backend.application.adapter.in.web.dto;

public record PlaceSearchResponse(
        String name,
        String roadAddress,
        String address,
        String link
) {
}
