package com.gumraze.rallyon.backend.identity.application.service;

import com.gumraze.rallyon.backend.common.exception.UnauthorizedException;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadLocalCredentialPort;
import com.gumraze.rallyon.backend.identity.application.port.out.PasswordHasherPort;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedIdentity;
import com.gumraze.rallyon.backend.identity.domain.IdentityRole;
import com.gumraze.rallyon.backend.identity.entity.IdentityAccount;
import com.gumraze.rallyon.backend.identity.entity.IdentityLocalCredential;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LocalIdentityAuthenticatorTest {

    @Mock
    private LoadLocalCredentialPort loadLocalCredentialPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @InjectMocks
    private LocalIdentityAuthenticator service;

    @Test
    @DisplayName("로컬 인증은 이메일을 정규화해 credential을 조회한다")
    void authenticate_normalizes_email_and_returns_authenticated_identity() {
        UUID identityAccountId = UUID.randomUUID();
        IdentityLocalCredential credential = credential(identityAccountId, "user@rallyon.local", "hashed-password");
        given(loadLocalCredentialPort.loadByEmailNormalized("user@rallyon.local")).willReturn(Optional.of(credential));
        given(passwordHasherPort.matches("password123!", "hashed-password")).willReturn(true);

        AuthenticatedIdentity result = service.authenticate(" User@RallyOn.Local ", "password123!");

        assertThat(result.identityAccountId()).isEqualTo(identityAccountId);
        assertThat(result.role()).isEqualTo(IdentityRole.USER);
        assertThat(result.displayName()).isNull();
    }

    @Test
    @DisplayName("credential이 없으면 인증에 실패한다")
    void authenticate_throws_when_credential_does_not_exist() {
        given(loadLocalCredentialPort.loadByEmailNormalized("user@rallyon.local")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("user@rallyon.local", "password123!"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 인증에 실패한다")
    void authenticate_throws_when_password_does_not_match() {
        UUID identityAccountId = UUID.randomUUID();
        IdentityLocalCredential credential = credential(identityAccountId, "user@rallyon.local", "hashed-password");
        given(loadLocalCredentialPort.loadByEmailNormalized("user@rallyon.local")).willReturn(Optional.of(credential));
        given(passwordHasherPort.matches("password123!", "hashed-password")).willReturn(false);

        assertThatThrownBy(() -> service.authenticate("user@rallyon.local", "password123!"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    private IdentityLocalCredential credential(UUID identityAccountId, String emailNormalized, String passwordHash) {
        IdentityAccount identityAccount = IdentityAccount.create();
        ReflectionTestUtils.setField(identityAccount, "id", identityAccountId);

        IdentityLocalCredential credential = IdentityLocalCredential.issue(identityAccount, emailNormalized, passwordHash);
        ReflectionTestUtils.setField(credential, "identityAccountId", identityAccountId);
        return credential;
    }
}
