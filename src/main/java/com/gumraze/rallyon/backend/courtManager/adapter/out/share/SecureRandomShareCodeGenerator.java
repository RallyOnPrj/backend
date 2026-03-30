package com.gumraze.rallyon.backend.courtManager.adapter.out.share;

import com.gumraze.rallyon.backend.courtManager.domain.share.ShareCodeGenerator;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecureRandomShareCodeGenerator implements ShareCodeGenerator {

    // 난수 생성기
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        // 16바이트(128 비트) 길이의 랜덤 바이트 배열 생성
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);

        // 생성한 바이트 배열을 URL에 들어갈 수 있는 문자열로 변환함.
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
