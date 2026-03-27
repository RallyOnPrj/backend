package com.gumraze.rallyon.backend.place.adapter.in.web;

import com.gumraze.rallyon.backend.application.adapter.in.web.PlaceSearchController;
import com.gumraze.rallyon.backend.application.port.in.SearchPlacesUseCase;
import com.gumraze.rallyon.backend.domain.PlaceSearchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PlaceSearchController.class,
        properties = "naver.search.local.enabled=true"
)
@AutoConfigureMockMvc(addFilters = false)
class PlaceSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchPlacesUseCase searchPlacesUseCase;

    @Test
    @DisplayName("장소 검색 요청 시 200과 장소 목록을 반환한다")
    void search_returnsPlaceList() throws Exception {
        // given
        given(searchPlacesUseCase.search("숙지다목적체육관"))
                .willReturn(List.of(
                        new PlaceSearchResult(
                                "숙지다목적체육관",
                                "경기도 수원시 ...",
                                "경기도 수원시 ...",
                                "스포츠시설",
                                "1271234567",
                                "371234567",
                                ""
                        )
                ));

        // when & then
        mockMvc.perform(get("/places/search")
                        .param("query", "숙지다목적체육관"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("숙지다목적체육관"))
                .andExpect(jsonPath("$[0].roadAddress").value("경기도 수원시 ..."))
                .andExpect(jsonPath("$[0].address").value("경기도 수원시 ..."))
                .andExpect(jsonPath("$[0].link").value(""));

        then(searchPlacesUseCase).should().search("숙지다목적체육관");
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 배열을 반환한다")
    void search_returnsEmptyArray_whenNoResults() throws Exception {
        // given
        given(searchPlacesUseCase.search("없는장소"))
                .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/places/search")
                        .param("query", "없는장소"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        then(searchPlacesUseCase).should().search("없는장소");
    }

    @Test
    @DisplayName("query 파라미터가 없으면 400을 반환한다")
    void search_withoutQuery_returnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/places/search"))
                .andExpect(status().isBadRequest());

        then(searchPlacesUseCase).shouldHaveNoInteractions();
    }
}
