package com.gumraze.rallyon.backend.region.internal.web;

import com.gumraze.rallyon.backend.region.RegionDistrictSummary;
import com.gumraze.rallyon.backend.region.RegionProvinceSummary;
import com.gumraze.rallyon.backend.region.RegionReader;
import com.gumraze.rallyon.backend.security.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.gumraze.rallyon.backend.support.UuidTestFixtures.uuid;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegionController.class)
@Import(SecurityConfig.class)
class RegionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegionReader regionReader;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("province 목록 조회 API")
    void get_provinces() throws Exception {
        UUID provinceId1 = uuid(1);
        UUID provinceId2 = uuid(2);

        when(regionReader.getProvinces()).thenReturn(List.of(
                new RegionProvinceSummary(provinceId1, "서울특별시", "11"),
                new RegionProvinceSummary(provinceId2, "경기도", "41")
        ));

        mockMvc.perform(get("/regions/provinces").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(provinceId1.toString()))
                .andExpect(jsonPath("$[0].name").value("서울특별시"))
                .andExpect(jsonPath("$[1].id").value(provinceId2.toString()))
                .andExpect(jsonPath("$[1].name").value("경기도"));
    }

    @Test
    @DisplayName("district 목록 조회 API")
    void get_districts_by_province_id() throws Exception {
        UUID provinceId = uuid(1);
        UUID districtId1 = uuid(11);
        UUID districtId2 = uuid(22);

        when(regionReader.getDistricts(provinceId)).thenReturn(List.of(
                new RegionDistrictSummary(districtId1, provinceId, "장안구", "41111"),
                new RegionDistrictSummary(districtId2, provinceId, "권선구", "41113")
        ));

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
