package com.gumraze.rallyon.backend.user.domain;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class UserProfileTagGenerator {

    private static final String TAG_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int idx = secureRandom.nextInt(TAG_CHARS.length());
            sb.append(TAG_CHARS.charAt(idx));
        }
        return sb.toString();
    }
}
