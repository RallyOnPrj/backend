package com.gumraze.rallyon.backend.user.entity;

import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileUpdateRequest {
    private Grade regionalGrade;
    private Grade nationalGrade;
    private String birth;
    private Boolean birthVisible;
    private UUID districtId;
    private String profileImageUrl;
    private Gender gender;
}
