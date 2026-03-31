package com.gumraze.rallyon.backend.place.application.service;

import com.gumraze.rallyon.backend.application.port.out.PlaceSearchPort;
import com.gumraze.rallyon.backend.application.service.PlaceSearchService;
import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import com.gumraze.rallyon.backend.domain.PlaceSearchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class PlaceSearchServiceTest {

    @Mock private PlaceSearchPort placeSearchPort;
    @Mock private ObjectProvider<PlaceSearchPort> placeSearchPortProvider;
    @InjectMocks private PlaceSearchService placeSearchService;

    @Test
    @DisplayName("검색어가 공백이면 빈 배열을 반환한다.")
    void search_blankQuery_returnsEmptyList() {
        // given
        String query = "   ";

        // when
        List<PlaceSearchResult> result = placeSearchService.search(query);

        // then
        assertThat(result).isEmpty();
        then(placeSearchPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("검색어를 trim한 뒤 장소 검색 포트에 위임한다.")
    void search_validQuery_delegatesToPort() {
        // given
        String query = "   숙지다목적체육관  ";
        List<PlaceSearchResult> expected = List.of(
                new PlaceSearchResult(
                        "숙지다목적체육관",
                        "도로명",
                        "지번",
                        "체육시설",
                        "127",
                        "37",
                        "link"
                )
        );

        given(placeSearchPortProvider.getIfAvailable()).willReturn(placeSearchPort);
        given(placeSearchPort.search(eq("숙지다목적체육관"), anyInt()))
                .willReturn(expected);

        // when
        List<PlaceSearchResult> result = placeSearchService.search(query);

        // then
        assertThat(result).isEqualTo(expected);
        then(placeSearchPort).should().search(eq("숙지다목적체육관"), anyInt());
    }

    @Test
    @DisplayName("유효한 검색어는 기본 검색 개수 5로 조회한다")
    void search_usesDefaultLimitOfTen() {
        // given
        String query = "숙지다목적체육관";
        given(placeSearchPortProvider.getIfAvailable()).willReturn(placeSearchPort);
        given(placeSearchPort.search("숙지다목적체육관", 5))
                .willReturn(List.of());

        // when
        placeSearchService.search(query);

        // then
        then(placeSearchPort).should().search("숙지다목적체육관", 5);
    }

    @Test
    @DisplayName("검색어가 2글자 미만이면 빈 배열을 반환한다.")
    void search_shortQuery_returnsEmptyList() {
        // given
        String query = "수";

        // when
        List<PlaceSearchResult> result = placeSearchService.search(query);

        // then
        assertThat(result).isEmpty();
        then(placeSearchPort).shouldHaveNoInteractions();
    }


    @Test
    @DisplayName("검색어가 null이면 빈 배열을 반환한다.")
    void search_nullQuery_returnsEmptyList() {
        // given
        String query = null;

        // when
        List<PlaceSearchResult> result = placeSearchService.search(query);

        // then
        assertThat(result).isEmpty();
        then(placeSearchPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("장소 검색 포트가 없으면 503 예외를 던진다")
    void search_without_place_search_port_throws_service_unavailable() {
        given(placeSearchPortProvider.getIfAvailable()).willReturn(null);

        assertThatThrownBy(() -> placeSearchService.search("숙지다목적체육관"))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("장소 검색 서비스를 현재 사용할 수 없습니다.");

        then(placeSearchPort).shouldHaveNoInteractions();
    }
}
