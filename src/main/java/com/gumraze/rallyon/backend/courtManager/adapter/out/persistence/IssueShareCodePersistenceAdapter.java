package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.out.IssueShareCodePort;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameRepository;
import com.gumraze.rallyon.backend.courtManager.domain.share.ShareCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IssueShareCodePersistenceAdapter implements IssueShareCodePort {

    private static final int MAX_SHARE_CODE_ATTEMPTS = 10;

    private final ShareCodeGenerator shareCodeGenerator;
    private final GameRepository gameRepository;

    @Override
    public String issue() {
        for (int attempt = 0; attempt < MAX_SHARE_CODE_ATTEMPTS; attempt++) {
            String shareCode = shareCodeGenerator.generate();
            if (!gameRepository.existsByShareCode(shareCode)) {
                return shareCode;
            }
        }

        throw new IllegalStateException("고유한 shareCode 생성에 실패했습니다.");
    }
}
