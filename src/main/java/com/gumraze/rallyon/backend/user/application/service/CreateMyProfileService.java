package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.user.application.port.in.CreateMyProfileUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.command.CreateMyProfileCommand;
import com.gumraze.rallyon.backend.user.application.port.out.*;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.domain.UserProfileTagGenerator;
import com.gumraze.rallyon.backend.user.dto.UserProfileCreateRequest;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.entity.UserGradeHistory;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import com.gumraze.rallyon.backend.user.service.UserProfileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateMyProfileService implements CreateMyProfileUseCase {

    private final LoadIdentityUserPort loadIdentityUserPort;
    private final LoadUserProfilePort loadUserProfilePort;
    private final SaveUserProfilePort saveUserProfilePort;
    private final LoadRegionPort loadRegionPort;
    private final SaveUserGradeHistoryPort saveUserGradeHistoryPort;
    private final UserProfileValidator userProfileValidator;
    private final UserProfileTagGenerator userProfileTagGenerator;

    @Override
    public void create(CreateMyProfileCommand command) {
        if (loadUserProfilePort.existsByUserId(command.userId())) {
            throw new IllegalArgumentException("이미 프로필이 존재합니다.");
        }

        User user = loadIdentityUserPort.loadById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        userProfileValidator.validateForCreate(new UserProfileCreateRequest(
                command.nickname(),
                command.districtId(),
                command.regionalGrade(),
                command.nationalGrade(),
                command.birth(),
                command.gender()
        ));

        UUID districtId = loadRegionPort.loadDistrictReference(command.districtId())
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."))
                .districtId();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .nickname(command.nickname())
                .districtId(districtId)
                .regionalGrade(command.regionalGrade())
                .nationalGrade(command.nationalGrade())
                .birth(parseBirth(command.birth()))
                .gender(command.gender())
                .tag(userProfileTagGenerator.generate())
                .tagChangedAt(LocalDateTime.now())
                .build();

        if (command.regionalGrade() != null) {
            saveUserGradeHistoryPort.save(new UserGradeHistory(user, command.regionalGrade(), GradeType.REGIONAL));
        }
        if (command.nationalGrade() != null) {
            saveUserGradeHistoryPort.save(new UserGradeHistory(user, command.nationalGrade(), GradeType.NATIONAL));
        }

        user.setStatus(UserStatus.ACTIVE);
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
