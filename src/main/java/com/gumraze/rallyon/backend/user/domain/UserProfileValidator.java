package com.gumraze.rallyon.backend.user.domain;

import com.gumraze.rallyon.backend.user.application.port.in.command.CreateMyProfileCommand;
import com.gumraze.rallyon.backend.user.application.port.in.command.UpdateMyProfileCommand;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Component
public class UserProfileValidator {

    public void validateForCreate(CreateMyProfileCommand command) {
        validateRequiredNickname(command.nickname());
        validateRequiredBirth(command.birth());
        validateBirthFormat(command.birth());
        validateRequiredGender(command.gender());
        validateRequiredDistrictId(command.districtId());
    }

    public void validateForUpdate(UpdateMyProfileCommand command) {
        validateOptionalNickname(command.nickname());
        validateOptionalBirth(command.birth());
        validateOptionalTag(command.tag());
    }

    public LocalDateTime parseBirthStartOfDay(String birth) {
        validateRequiredBirth(birth);
        validateBirthFormat(birth);
        LocalDate parsed = LocalDate.parse(
                birth,
                DateTimeFormatter.BASIC_ISO_DATE.withLocale(Locale.KOREA)
        );
        return parsed.atStartOfDay();
    }

    public String normalizeOptionalTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return null;
        }
        return tag.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private void validateRequiredNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname이 필요합니다.");
        }
    }

    private void validateOptionalNickname(String nickname) {
        if (nickname != null && nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 비워둘 수 없습니다.");
        }
    }

    private void validateRequiredBirth(String birth) {
        if (birth == null || birth.isBlank()) {
            throw new IllegalArgumentException("Birth가 필요합니다.");
        }
    }

    private void validateOptionalBirth(String birth) {
        if (birth == null) {
            return;
        }
        if (birth.isBlank()) {
            throw new IllegalArgumentException("Birth는 비워둘 수 없습니다.");
        }
        validateBirthFormat(birth);
    }

    private void validateBirthFormat(String birth) {
        try {
            LocalDate.parse(
                    birth,
                    DateTimeFormatter.BASIC_ISO_DATE.withLocale(Locale.KOREA)
            );
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Birth 형식이 올바르지 않습니다.");
        }
    }

    private void validateRequiredGender(Object gender) {
        if (gender == null) {
            throw new IllegalArgumentException("gender가 필요합니다.");
        }
    }

    private void validateRequiredDistrictId(Object districtId) {
        if (districtId == null) {
            throw new IllegalArgumentException("districtId가 필요합니다.");
        }
    }

    private void validateOptionalTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return;
        }

        String normalizedTag = normalizeOptionalTag(tag);
        if (normalizedTag == null || normalizedTag.length() != 4) {
            throw new IllegalArgumentException("태그는 4글자로 입력해야 합니다.");
        }
    }
}
