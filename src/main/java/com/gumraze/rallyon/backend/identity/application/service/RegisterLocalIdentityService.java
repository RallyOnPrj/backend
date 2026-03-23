package com.gumraze.rallyon.backend.identity.application.service;

import com.gumraze.rallyon.backend.common.exception.ConflictException;
import com.gumraze.rallyon.backend.identity.application.port.in.RegisterLocalIdentityUseCase;
import com.gumraze.rallyon.backend.identity.application.port.in.command.RegisterLocalIdentityCommand;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadLocalCredentialPort;
import com.gumraze.rallyon.backend.identity.application.port.out.PasswordHasherPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveIdentityAccountPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveLocalCredentialPort;
import com.gumraze.rallyon.backend.identity.domain.authentication.EmailNormalizer;
import com.gumraze.rallyon.backend.identity.domain.authentication.PasswordPolicy;
import com.gumraze.rallyon.backend.identity.entity.IdentityLocalCredential;
import com.gumraze.rallyon.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterLocalIdentityService implements RegisterLocalIdentityUseCase {

    private final SaveIdentityAccountPort saveIdentityAccountPort;
    private final LoadLocalCredentialPort loadLocalCredentialPort;
    private final SaveLocalCredentialPort saveLocalCredentialPort;
    private final PasswordHasherPort passwordHasherPort;

    @Override
    public UUID register(RegisterLocalIdentityCommand command) {
        String normalizedEmail = EmailNormalizer.normalize(command.email());
        PasswordPolicy.validate(command.password());

        if (loadLocalCredentialPort.loadByEmailNormalized(normalizedEmail).isPresent()) {
            throw new ConflictException("이미 가입된 이메일입니다.");
        }

        User user = saveIdentityAccountPort.save(User.builder().build());
        saveLocalCredentialPort.save(IdentityLocalCredential.builder()
                .user(user)
                .emailNormalized(normalizedEmail)
                .passwordHash(passwordHasherPort.hash(command.password()))
                .build());
        return user.getId();
    }
}
