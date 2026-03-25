package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.common.exception.ConflictException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.common.exception.UnprocessableEntityException;
import com.gumraze.rallyon.backend.user.application.port.in.UpdateMyProfileUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.command.UpdateMyProfileCommand;
import com.gumraze.rallyon.backend.user.application.port.out.LoadRegionPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.application.port.out.SaveUserProfilePort;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import com.gumraze.rallyon.backend.user.domain.UserProfileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateMyProfileService implements UpdateMyProfileUseCase {

    private final LoadUserProfilePort loadUserProfilePort;
    private final SaveUserProfilePort saveUserProfilePort;
    private final LoadRegionPort loadRegionPort;
    private final UserProfileValidator userProfileValidator;

    @Override
    public void update(UpdateMyProfileCommand command) {
        userProfileValidator.validateForUpdate(command);

        UserProfile profile = loadUserProfilePort.loadByIdentityAccountId(command.userId())
                .orElseThrow(() -> new NotFoundException("사용자의 프로필을 찾을 수 없습니다."));

        applyPublicIdentityChanges(profile, command);

        if (command.regionalGrade() != null) {
            profile.changeRegionalGrade(command.regionalGrade());
        }
        if (command.nationalGrade() != null) {
            profile.changeNationalGrade(command.nationalGrade());
        }
        if (command.birth() != null) {
            profile.changeBirth(userProfileValidator.parseBirthStartOfDay(command.birth()));
        }
        if (command.birthVisible() != null) {
            profile.changeBirthVisible(command.birthVisible());
        }
        if (command.districtId() != null) {
            profile.changeDistrictId(loadRegionPort.loadDistrictReference(command.districtId())
                    .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."))
                    .districtId());
        }
        if (command.profileImageUrl() != null) {
            profile.changeProfileImageUrl(command.profileImageUrl());
        }
        if (command.gender() != null) {
            profile.changeGender(command.gender());
        }

        saveUserProfilePort.save(profile);
    }

    private void applyPublicIdentityChanges(UserProfile profile, UpdateMyProfileCommand command) {
        String requestedNickname = command.nickname();
        String normalizedTag = userProfileValidator.normalizeOptionalTag(command.tag());

        boolean nicknameRequested = requestedNickname != null;
        boolean tagRequested = normalizedTag != null;

        if (!nicknameRequested && !tagRequested) {
            return;
        }

        String finalNickname = nicknameRequested ? requestedNickname : profile.getNickname();
        String finalTag = tagRequested ? normalizedTag : profile.getTag();

        if (tagRequested) {
            LocalDateTime lastChanged = profile.getTagChangedAt();
            if (lastChanged != null && lastChanged.isAfter(LocalDateTime.now().minusDays(90))) {
                throw new UnprocessableEntityException("태그 변경은 90일 이내에 한 번만 가능합니다.");
            }
        }

        loadUserProfilePort.loadByNicknameAndTag(finalNickname, finalTag)
                .filter(existing -> !existing.getIdentityAccountId().equals(command.userId()))
                .ifPresent(existing -> {
                    throw new ConflictException("이미 존재하는 닉네임과 태그입니다.");
                });

        if (nicknameRequested && !finalNickname.equals(profile.getNickname())) {
            profile.changeNickname(finalNickname);
        }
        if (tagRequested && !finalTag.equals(profile.getTag())) {
            profile.changeTag(finalTag, LocalDateTime.now());
        }
    }
}
