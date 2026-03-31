package com.gumraze.rallyon.backend.application.adapter.out.naver;

import com.gumraze.rallyon.backend.application.adapter.out.naver.dto.NaverLocalSearchResponse;
import com.gumraze.rallyon.backend.application.port.out.PlaceSearchPort;
import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import com.gumraze.rallyon.backend.domain.PlaceSearchResult;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "naver.search.local", name = "enabled", havingValue = "true")
public class NaverPlaceSearchAdapter implements PlaceSearchPort {

    private final RestClient.Builder restClientBuilder;
    private final NaverPlaceSearchProperties properties;

    @Override
    public List<PlaceSearchResult> search(String query, int limit) {
        if (properties.clientId() == null || properties.clientId().isBlank()
                || properties.clientSecret() == null || properties.clientSecret().isBlank()) {
            throw new ServiceUnavailableException("장소 검색 서비스를 현재 사용할 수 없습니다.");
        }

        RestClient restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .build();

        NaverLocalSearchResponse response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/search/local.json")
                            .queryParam("query", query)
                            .queryParam("display", limit)
                            .queryParam("start", 1)
                            .queryParam("sort", "random")
                            .build())
                    .header("X-Naver-Client-Id", properties.clientId())
                    .header("X-Naver-Client-Secret", properties.clientSecret())
                    .retrieve()
                    .body(NaverLocalSearchResponse.class);
        } catch (RestClientException ex) {
            log.error("Naver place search failed. query={}", query, ex);
            throw new ServiceUnavailableException("장소 검색 서비스를 현재 사용할 수 없습니다.", ex);
        }

        if (response == null || response.items() == null || response.items().isEmpty()) {
            return List.of();
        }

        return response.items().stream()
                .map(item -> new PlaceSearchResult(
                        stripHtml(item.title()),
                        item.roadAddress(),
                        item.address(),
                        item.category(),
                        item.mapx() == null ? "" : String.valueOf(item.mapx()),
                        item.mapy() == null ? "" : String.valueOf(item.mapy()),
                        item.link()
                ))
                .toList();
    }

    private String stripHtml(String value) {
        if (value == null) return "";

        return value.replaceAll("<[^>]*>", "");
    }
}
