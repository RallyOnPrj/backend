package com.gumraze.rallyon.backend.application.adapter.out.naver.dto;

import java.util.List;

public record NaverLocalSearchResponse(
        String lastBuildDate,
        int total,
        int start,
        List<Item> items
) {
    public record Item(
            String title,
            String link,
            String category,
            String description,
            String telephone,
            String address,
            String roadAddress,
            Integer mapx,
            Integer mapy
    ) { }
}
