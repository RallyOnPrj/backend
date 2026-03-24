package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.common.exception.ConflictException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.common.exception.UnprocessableEntityException;
import com.gumraze.rallyon.backend.user.application.port.in.UpdateMyPublicIdentityUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.command.UpdateMyPublicIdentityCommand;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.application.port.out.SaveUserProfilePort;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateMyPublicIdentityService implements UpdateMyPublicIdentityUseCase {

    private final LoadUserProfilePort loadUserProfilePort;
    private final SaveUserProfilePort saveUserProfilePort;

    @Override
    public void update(UpdateMyPublicIdentityCommand command) {
        UserProfile profile = loadUserProfilePort.loadByUserId(command.userId())
                .orElseThrow(() -> new NotFoundException("사용자의 프로필을 찾을 수 없습니다."));

        String newNickname = command.nickname();
        String newTagRaw = command.tag();
        boolean nicknameRequested = newNickname != null && !newNickname.isBlank();
        boolean tagRequested = newTagRaw != null && !newTagRaw.isBlank();

        if (!nicknameRequested && !tagRequested) {
            throw new IllegalArgumentException("닉네임 또는 태그가 필요합니다.");
        }

        String finalNickname = nicknameRequested ? newNickname : profile.getNickname();
        String normalizedTag = tagRequested
                ? newTagRaw.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT)
                : profile.getTag();

        if (tagRequested && normalizedTag.length() != 4) {
            throw new IllegalArgumentException("태그는 4글자로 입력해야 합니다.");
        }

        boolean nicknameChanged = !finalNickname.equals(profile.getNickname());
        boolean tagChanged = !normalizedTag.equals(profile.getTag());

        if (tagChanged) {
            LocalDateTime lastChanged = profile.getTagChangedAt();
            if (lastChanged != null && lastChanged.isAfter(LocalDateTime.now().minusDays(90))) {
                throw new UnprocessableEntityException("태그 변경은 90일 이내에 한 번만 가능합니다.");
            }
        }

        loadUserProfilePort.loadByNicknameAndTag(finalNickname, normalizedTag)
                .filter(existing -> !existing.getUser().getId().equals(command.userId()))
                .ifPresent(existing -> {
                    throw new ConflictException("이미 존재하는 닉네임과 태그입니다.");
                });

        if (nicknameChanged) {
            profile.setNickname(finalNickname);
        }
        if (tagChanged) {
            profile.setTag(normalizedTag);
            profile.setTagChangedAt(LocalDateTime.now());
        }

        saveUserProfilePort.save(profile);
    }
}
