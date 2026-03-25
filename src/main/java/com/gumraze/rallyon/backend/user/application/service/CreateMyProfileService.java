package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.user.application.port.in.CreateMyProfileUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.command.CreateMyProfileCommand;
import com.gumraze.rallyon.backend.user.application.port.out.*;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import com.gumraze.rallyon.backend.user.domain.UserProfileTagGenerator;
import com.gumraze.rallyon.backend.user.entity.UserGradeHistory;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import com.gumraze.rallyon.backend.user.domain.UserProfileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateMyProfileService implements CreateMyProfileUseCase {

    private final LoadUserProfilePort loadUserProfilePort;
    private final SaveUserProfilePort saveUserProfilePort;
    private final LoadRegionPort loadRegionPort;
    private final SaveUserGradeHistoryPort saveUserGradeHistoryPort;
    private final UserProfileValidator userProfileValidator;
    private final UserProfileTagGenerator userProfileTagGenerator;

    @Override
    public void create(CreateMyProfileCommand command) {
        if (loadUserProfilePort.existsByIdentityAccountId(command.userId())) {
            throw new IllegalArgumentException("이미 프로필이 존재합니다.");
        }

        userProfileValidator.validateForCreate(command);

        UUID districtId = loadRegionPort.loadDistrictReference(command.districtId())
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."))
                .districtId();

        UserProfile profile = UserProfile.create(
                command.userId(),
                command.nickname(),
                districtId,
                command.regionalGrade(),
                command.nationalGrade(),
                userProfileValidator.parseBirthStartOfDay(command.birth()),
                command.gender(),
                userProfileTagGenerator.generate(),
                LocalDateTime.now()
        );

        if (command.regionalGrade() != null) {
            saveUserGradeHistoryPort.save(UserGradeHistory.record(command.userId(), command.regionalGrade(), GradeType.REGIONAL));
        }
        if (command.nationalGrade() != null) {
            saveUserGradeHistoryPort.save(UserGradeHistory.record(command.userId(), command.nationalGrade(), GradeType.NATIONAL));
        }

        saveUserProfilePort.save(profile);
    }
}
