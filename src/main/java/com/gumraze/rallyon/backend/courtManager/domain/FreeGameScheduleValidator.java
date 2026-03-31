package com.gumraze.rallyon.backend.courtManager.domain;

import com.gumraze.rallyon.backend.common.exception.UnprocessableEntityException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Component
public class FreeGameScheduleValidator {

    public LocalDateTime parseRequiredFuture(String scheduledAt) {
        if (scheduledAt == null || scheduledAt.isBlank()) {
            throw new IllegalArgumentException("scheduledAt이 필요합니다.");
        }

        return parseFuture(scheduledAt);
    }

    public LocalDateTime parseOptionalFuture(String scheduledAt) {
        if (scheduledAt == null) {
            return null;
        }

        if (scheduledAt.isBlank()) {
            throw new IllegalArgumentException("scheduledAt은 비워둘 수 없습니다.");
        }

        return parseFuture(scheduledAt);
    }

    private LocalDateTime parseFuture(String scheduledAt) {
        LocalDateTime parsed;
        try {
            parsed = LocalDateTime.parse(scheduledAt);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("scheduledAt 형식이 올바르지 않습니다.");
        }

        if (!parsed.isAfter(LocalDateTime.now())) {
            throw new UnprocessableEntityException("scheduledAt은 현재 시각보다 미래여야 합니다.");
        }

        return parsed;
    }
}
