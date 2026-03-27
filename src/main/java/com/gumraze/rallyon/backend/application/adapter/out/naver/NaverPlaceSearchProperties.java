package com.gumraze.rallyon.backend.application.adapter.out.naver;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.search.local")
public record NaverPlaceSearchProperties(
        boolean enabled,
        String clientId,
        String clientSecret,
        String baseUrl
) {
}
