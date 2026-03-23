package com.gumraze.rallyon.backend.region.internal.service;

import com.gumraze.rallyon.backend.region.RegionDistrictReference;
import com.gumraze.rallyon.backend.region.RegionDistrictSummary;
import com.gumraze.rallyon.backend.region.RegionProvinceSummary;
import com.gumraze.rallyon.backend.region.internal.persistence.entity.RegionDistrict;
import com.gumraze.rallyon.backend.region.internal.persistence.entity.RegionProvince;
import com.gumraze.rallyon.backend.region.internal.persistence.repository.RegionDistrictRepository;
import com.gumraze.rallyon.backend.region.internal.persistence.repository.RegionProvinceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.gumraze.rallyon.backend.support.UuidTestFixtures.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegionReaderImplTest {

    @Mock
    private RegionProvinceRepository regionProvinceRepository;

    @Mock
    private RegionDistrictRepository regionDistrictRepository;

    @InjectMocks
    private RegionReaderImpl regionReader;

    @Test
    @DisplayName("province 목록을 code 오름차순으로 조회한다")
    void get_provinces_sorted_by_code() {
        RegionProvince p1 = mock(RegionProvince.class);
        RegionProvince p2 = mock(RegionProvince.class);
        RegionProvince p3 = mock(RegionProvince.class);

        when(p1.getId()).thenReturn(uuid(11));
        when(p1.getName()).thenReturn("서울특별시");
        when(p1.getCode()).thenReturn("11");
        when(p2.getId()).thenReturn(uuid(22));
        when(p2.getName()).thenReturn("부산광역시");
        when(p2.getCode()).thenReturn("26");
        when(p3.getId()).thenReturn(uuid(33));
        when(p3.getName()).thenReturn("강원특별자치도");
        when(p3.getCode()).thenReturn("02");

        when(regionProvinceRepository.findAll()).thenReturn(List.of(p2, p1, p3));

        List<RegionProvinceSummary> result = regionReader.getProvinces();

        assertThat(result).extracting(RegionProvinceSummary::id)
                .containsExactly(uuid(33), uuid(11), uuid(22));
    }

    @Test
    @DisplayName("provinceId를 받아 district 목록을 code 오름차순으로 조회한다")
    void get_districts_sorted_by_code() {
        UUID provinceId = uuid(1);
        RegionProvince province = mock(RegionProvince.class);
        when(province.getId()).thenReturn(provinceId);

        RegionDistrict d1 = mock(RegionDistrict.class);
        RegionDistrict d2 = mock(RegionDistrict.class);
        RegionDistrict d3 = mock(RegionDistrict.class);

        when(d1.getId()).thenReturn(uuid(11));
        when(d1.getName()).thenReturn("장안구");
        when(d1.getCode()).thenReturn("41111");
        when(d1.getProvince()).thenReturn(province);

        when(d2.getId()).thenReturn(uuid(22));
        when(d2.getName()).thenReturn("강남구");
        when(d2.getCode()).thenReturn("11680");
        when(d2.getProvince()).thenReturn(province);

        when(d3.getId()).thenReturn(uuid(33));
        when(d3.getName()).thenReturn("춘천시");
        when(d3.getCode()).thenReturn("42150");
        when(d3.getProvince()).thenReturn(province);

        when(regionDistrictRepository.findAllByProvinceId(provinceId)).thenReturn(List.of(d3, d1, d2));

        List<RegionDistrictSummary> result = regionReader.getDistricts(provinceId);

        assertThat(result).extracting(RegionDistrictSummary::id)
                .containsExactly(uuid(22), uuid(11), uuid(33));
    }

    @Test
    @DisplayName("districtId로 district/province 참조 정보를 조회한다")
    void find_district_reference() {
        UUID provinceId = uuid(1);
        UUID districtId = uuid(2);

        RegionProvince province = mock(RegionProvince.class);
        when(province.getId()).thenReturn(provinceId);
        when(province.getName()).thenReturn("경기도");
        when(province.getCode()).thenReturn("41");

        RegionDistrict district = mock(RegionDistrict.class);
        when(district.getId()).thenReturn(districtId);
        when(district.getName()).thenReturn("권선구");
        when(district.getCode()).thenReturn("41113");
        when(district.getProvince()).thenReturn(province);

        when(regionDistrictRepository.findById(districtId)).thenReturn(Optional.of(district));

        RegionDistrictReference result = regionReader.findDistrictReference(districtId).orElseThrow();

        assertThat(result.districtId()).isEqualTo(districtId);
        assertThat(result.districtName()).isEqualTo("권선구");
        assertThat(result.provinceId()).isEqualTo(provinceId);
        assertThat(result.provinceName()).isEqualTo("경기도");
    }
}
