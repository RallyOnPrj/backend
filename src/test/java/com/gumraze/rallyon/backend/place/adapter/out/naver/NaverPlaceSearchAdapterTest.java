package com.gumraze.rallyon.backend.place.adapter.out.naver;

import com.gumraze.rallyon.backend.application.adapter.out.naver.NaverPlaceSearchAdapter;
import com.gumraze.rallyon.backend.application.adapter.out.naver.NaverPlaceSearchProperties;
import com.gumraze.rallyon.backend.domain.PlaceSearchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class NaverPlaceSearchAdapterTest {

    @Test
    @DisplayName("네이버 지역 검색 응답을 장소 검색 결과로 변환한다")
    void search_mapsNaverItemsToPlaceSearchResults() {
        // given
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();

        NaverPlaceSearchProperties properties = new NaverPlaceSearchProperties(
                true,
                "client-id",
                "client-secret",
                "https://openapi.naver.com"
        );

        NaverPlaceSearchAdapter adapter = new NaverPlaceSearchAdapter(restClientBuilder, properties);

        server.expect(requestTo("https://openapi.naver.com/v1/search/local.json?query=%EC%88%99%EC%A7%80%EB%8B%A4%EB%AA%A9%EC%A0%81%EC%B2%B4%EC%9C%A1%EA%B4%80&display=10&start=1&sort=random"))
                .andExpect(method(GET))
                .andExpect(header("X-Naver-Client-Id", "client-id"))
                .andExpect(header("X-Naver-Client-Secret", "client-secret"))
                .andRespond(withSuccess("""
                        {
                          "lastBuildDate": "Mon, 16 Mar 2026 10:00:00 +0900",
                          "total": 1,
                          "start": 1,
                          "display": 1,
                          "items": [
                            {
                              "title": "<b>숙지다목적체육관</b>",
                              "link": "",
                              "category": "스포츠시설",
                              "description": "",
                              "telephone": "",
                              "address": "경기도 수원시 ...",
                              "roadAddress": "경기도 수원시 ...",
                              "mapx": 127123456,
                              "mapy": 37123456
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        // when
        List<PlaceSearchResult> results = adapter.search("숙지다목적체육관", 10);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst()).isEqualTo(new PlaceSearchResult(
                "숙지다목적체육관",
                "경기도 수원시 ...",
                "경기도 수원시 ...",
                "스포츠시설",
                "127123456",
                "37123456",
                ""
        ));

        server.verify();
    }

    @Test
    @DisplayName("네이버 응답 title의 HTML 태그를 제거한다")
    void search_stripsHtmlFromTitle() {
        // given
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();

        NaverPlaceSearchProperties properties = new NaverPlaceSearchProperties(
                true,
                "client-id",
                "client-secret",
                "https://openapi.naver.com"
        );

        NaverPlaceSearchAdapter adapter = new NaverPlaceSearchAdapter(restClientBuilder, properties);

        server.expect(requestTo("https://openapi.naver.com/v1/search/local.json?query=%EC%88%99%EC%A7%80%EB%8B%A4%EB%AA%A9%EC%A0%81%EC%B2%B4%EC%9C%A1%EA%B4%80&display=10&start=1&sort=random"))
                .andRespond(withSuccess("""
                        {
                          "lastBuildDate": "Mon, 16 Mar 2026 10:00:00 +0900",
                          "total": 1,
                          "start": 1,
                          "display": 1,
                          "items": [
                            {
                              "title": "<b>숙지다목적체육관</b>",
                              "link": "",
                              "category": "스포츠시설",
                              "description": "",
                              "telephone": "",
                              "address": "경기도 수원시 ...",
                              "roadAddress": "경기도 수원시 ...",
                              "mapx": 127123456,
                              "mapy": 37123456
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        // when
        List<PlaceSearchResult> results = adapter.search("숙지다목적체육관", 10);

        // then
        assertThat(results.getFirst().name()).isEqualTo("숙지다목적체육관");
    }

    @Test
    @DisplayName("네이버 응답 items가 없으면 빈 배열을 반환한다")
    void search_returnsEmptyListWhenItemsAreMissing() {
        // given
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();

        NaverPlaceSearchProperties properties = new NaverPlaceSearchProperties(
                true,
                "client-id",
                "client-secret",
                "https://openapi.naver.com"
        );

        NaverPlaceSearchAdapter adapter = new NaverPlaceSearchAdapter(restClientBuilder, properties);

        server.expect(requestTo("https://openapi.naver.com/v1/search/local.json?query=%EC%88%99%EC%A7%80%EB%8B%A4%EB%AA%A9%EC%A0%81%EC%B2%B4%EC%9C%A1%EA%B4%80&display=10&start=1&sort=random"))
                .andRespond(withSuccess("""
                        {
                          "lastBuildDate": "Mon, 16 Mar 2026 10:00:00 +0900",
                          "total": 0,
                          "start": 1,
                          "display": 0,
                          "items": []
                        }
                        """, MediaType.APPLICATION_JSON));

        // when
        List<PlaceSearchResult> results = adapter.search("숙지다목적체육관", 10);

        // then
        assertThat(results).isEmpty();
    }
}
