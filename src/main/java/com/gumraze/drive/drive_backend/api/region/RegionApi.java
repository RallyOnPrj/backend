package com.gumraze.drive.drive_backend.api.region;

import com.gumraze.drive.drive_backend.region.dto.RegionDistrictResponseDto;
import com.gumraze.drive.drive_backend.region.dto.RegionProvinceResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Regions", description = "지역 조회 API")
public interface RegionApi {

    @Operation(
            summary = "시/도 조회",
            description = "전체 시/도 목록을 조회합니다.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "시/도 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegionProvinceResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류가 발생했습니다.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<List<RegionProvinceResponseDto>> getProvinces();

    @Operation(
            summary = "시/군/구 조회",
            description = "시/도 ID로 시/군/구 목록을 조회합니다.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "시/군/구 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegionDistrictResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 검증 실패",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류가 발생했습니다.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<List<RegionDistrictResponseDto>> getDistricts(
            @Parameter(description = "시/도 ID", required = true, example = "1")
            Long provinceId
    );
}
