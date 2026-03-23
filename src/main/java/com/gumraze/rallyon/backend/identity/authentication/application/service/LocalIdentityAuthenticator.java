package com.gumraze.rallyon.backend.identity.authentication.application.service;

import com.gumraze.rallyon.backend.common.exception.UnauthorizedException;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadLocalCredentialPort;
import com.gumraze.rallyon.backend.identity.application.port.out.PasswordHasherPort;
import com.gumraze.rallyon.backend.identity.authorizationserver.domain.IdentityAuthenticatedPrincipal;
import com.gumraze.rallyon.backend.identity.domain.authentication.EmailNormalizer;
import com.gumraze.rallyon.backend.identity.entity.IdentityLocalCredential;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LocalIdentityAuthenticator {

    private final LoadLocalCredentialPort loadLocalCredentialPort;
    private final PasswordHasherPort passwordHasherPort;

    public LocalIdentityAuthenticator(
            LoadLocalCredentialPort loadLocalCredentialPort,
            PasswordHasherPort passwordHasherPort
    ) {
        this.loadLocalCredentialPort = loadLocalCredentialPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    public IdentityAuthenticatedPrincipal authenticate(String email, String password) {
        String normalizedEmail = EmailNormalizer.normalize(email);
        IdentityLocalCredential credential = loadLocalCredentialPort.loadByEmailNormalized(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordHasherPort.matches(password, credential.getPasswordHash())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return new IdentityAuthenticatedPrincipal(
                credential.getUserId(),
                credential.getUser().getRole(),
                credential.getUser().getStatus(),
                null
        );
    }
}
