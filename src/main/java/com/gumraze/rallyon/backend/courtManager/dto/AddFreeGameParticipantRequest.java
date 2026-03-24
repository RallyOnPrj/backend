package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record AddFreeGameParticipantRequest(
        UUID userId,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z가-힣])[A-Za-z가-힣 ]+$",
                message = "참가자 이름은 한글 또는 영문만 입력할 수 있습니다."
        )
        String name,

        @NotNull
        Gender gender,

        @NotNull
        Grade grade,

        @NotNull
        @Min(10)
        @Max(70)
        Integer age
) {
}
