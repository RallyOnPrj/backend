package com.gumraze.rallyon.backend.application.adapter.in.web;

import com.gumraze.rallyon.backend.application.adapter.in.web.dto.PlaceSearchResponse;
import com.gumraze.rallyon.backend.application.port.in.SearchPlacesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceSearchController {

    private final SearchPlacesUseCase searchPlacesUseCase;

    @GetMapping("/search")
    public List<PlaceSearchResponse> search(
            @RequestParam String query
    ) {
        return searchPlacesUseCase.search(query).stream()
                .map(place -> new PlaceSearchResponse(
                        place.name(),
                        place.roadAddress(),
                        place.address(),
                        place.link()
                ))
                .toList();
    }

}
