package com.gumraze.rallyon.backend.identity.domain;

import java.util.Locale;

public final class EmailNormalizer {

    private EmailNormalizer() {
    }

    public static String normalize(String rawEmail) {
        if (rawEmail == null) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        String normalized = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || !normalized.contains("@")) {
            throw new IllegalArgumentException("유효한 이메일이 아닙니다.");
        }
        return normalized;
    }
}
