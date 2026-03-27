package com.gumraze.rallyon.backend.user.dto;

import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "사용자 프로필 생성 요청 DTO")
public record UserProfileCreateRequest(
        @Schema(description = "닉네임", example = "SYAN")
        String nickname,

        @Schema(description = "지역 ID")
        UUID districtId,

        @Schema(description = "지역 급수", example = "ROOKIE")
        Grade regionalGrade,

        @Schema(description = "전국 급수", example = "ROOKIE")
        Grade nationalGrade,

        @Schema(description = "생년월일", example = "19990101")
        String birth,

        @Schema(description = "성별", example = "MALE")
        Gender gender
) {
}
