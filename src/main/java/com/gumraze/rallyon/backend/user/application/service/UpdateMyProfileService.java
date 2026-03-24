package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.user.application.port.in.UpdateMyProfileUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.command.UpdateMyProfileCommand;
import com.gumraze.rallyon.backend.user.application.port.out.LoadRegionPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.application.port.out.SaveUserProfilePort;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateMyProfileService implements UpdateMyProfileUseCase {

    private final LoadUserProfilePort loadUserProfilePort;
    private final SaveUserProfilePort saveUserProfilePort;
    private final LoadRegionPort loadRegionPort;

    @Override
    public void update(UpdateMyProfileCommand command) {
        UserProfile profile = loadUserProfilePort.loadByUserId(command.userId())
                .orElseThrow(() -> new NotFoundException("사용자의 프로필을 찾을 수 없습니다."));

        if (command.regionalGrade() != null) {
            profile.setRegionalGrade(command.regionalGrade());
        }
        if (command.nationalGrade() != null) {
            profile.setNationalGrade(command.nationalGrade());
        }
        if (command.birth() != null) {
            profile.setBirth(parseBirth(command.birth()));
        }
        if (command.birthVisible() != null) {
            profile.setBirthVisible(command.birthVisible());
        }
        if (command.districtId() != null) {
            profile.setDistrictId(loadRegionPort.loadDistrictReference(command.districtId())
                    .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."))
                    .districtId());
        }
        if (command.profileImageUrl() != null) {
            profile.setProfileImageUrl(command.profileImageUrl());
        }
        if (command.gender() != null) {
            profile.setGender(command.gender());
        }

        saveUserProfilePort.save(profile);
    }

    private LocalDateTime parseBirth(String birth) {
        LocalDate parsed = LocalDate.parse(
                birth,
                DateTimeFormatter.BASIC_ISO_DATE.withLocale(Locale.KOREA)
        );
        return parsed.atStartOfDay();
    }
}
