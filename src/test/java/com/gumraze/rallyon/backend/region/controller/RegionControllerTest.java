package com.gumraze.rallyon.backend.region.controller;

import com.gumraze.rallyon.backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.rallyon.backend.config.SecurityConfig;
import com.gumraze.rallyon.backend.region.entity.RegionDistrict;
import com.gumraze.rallyon.backend.region.entity.RegionProvince;
import com.gumraze.rallyon.backend.region.service.RegionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.gumraze.rallyon.backend.support.UuidTestFixtures.uuid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegionController.class)     // RegionController 클래스만 로드
@Import(SecurityConfig.class)
public class RegionControllerTest {

    // Http요청 및 응답을 가짜로 테스트
    @Autowired
    private MockMvc mockMvc;

    // 서비스는 가짜로 주입
    @MockitoBean
    private RegionService regionService;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @Test
    @DisplayName("province 목록 조회 API")
    void get_provinces() throws Exception {
        // given: getProvinces()가 id와 name을 반환한다고 가정
        RegionProvince p1 = mock(RegionProvince.class);
        RegionProvince p2 = mock(RegionProvince.class);
        UUID provinceId1 = uuid(1);
        UUID provinceId2 = uuid(2);

        when(p1.getId()).thenReturn(provinceId1);
        when(p1.getName()).thenReturn("서울특별시");
        when(p2.getId()).thenReturn(provinceId2);
        when(p2.getName()).thenReturn("경기도");

        // regionService 요청 시
        when(regionService.getProvinces()).thenReturn(List.of(p1, p2));

        // when: /regions/provinces 요청
        mockMvc.perform(get("/regions/provinces")
                        .accept(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 배열 길이
                .andExpect(jsonPath("$.length()").value(2))
                // 첫 번째 province
                .andExpect(jsonPath("$[0].id").value(provinceId1.toString()))
                .andExpect(jsonPath("$[0].name").value("서울특별시"))
                // 두 번째 province
                .andExpect(jsonPath("$[1].id").value(provinceId2.toString()))
                .andExpect(jsonPath("$[1].name").value("경기도"));
    }

    @Test
    @DisplayName("district 목록 조회 API")
    void get_districts_by_province_id() throws Exception {
        // given: provinceId가 주어지면 해당 id에 대한 districts의 id와 name list를 반환함
        RegionDistrict d1 = mock(RegionDistrict.class);
        RegionDistrict d2 = mock(RegionDistrict.class);
        UUID provinceId = uuid(1);
        UUID districtId1 = uuid(1);
        UUID districtId2 = uuid(2);

        when(d1.getId()).thenReturn(districtId1);
        when(d1.getName()).thenReturn("장안구");
        when(d2.getId()).thenReturn(districtId2);
        when(d2.getName()).thenReturn("권선구");

        // getDistricts(provinceId) 요청
        when(regionService.getDistricts(provinceId)).thenReturn(List.of(d1, d2));

        // when: /regions/provinces/{id}/districts 요청
        mockMvc.perform(get("/regions/{provinceId}/districts", provinceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(districtId1.toString()))
                .andExpect(jsonPath("$[0].name").value("장안구"))
                .andExpect(jsonPath("$[1].id").value(districtId2.toString()))
                .andExpect(jsonPath("$[1].name").value("권선구"));
    }
}
