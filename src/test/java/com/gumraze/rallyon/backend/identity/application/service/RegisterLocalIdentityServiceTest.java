package com.gumraze.rallyon.backend.identity.application.service;

import com.gumraze.rallyon.backend.common.exception.ConflictException;
import com.gumraze.rallyon.backend.identity.application.port.in.command.RegisterLocalIdentityCommand;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadLocalCredentialPort;
import com.gumraze.rallyon.backend.identity.application.port.out.PasswordHasherPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveAccountPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveLocalCredentialPort;
import com.gumraze.rallyon.backend.identity.entity.Account;
import com.gumraze.rallyon.backend.identity.entity.LocalCredential;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterLocalIdentityServiceTest {

    @Mock
    private SaveAccountPort saveAccountPort;

    @Mock
    private LoadLocalCredentialPort loadLocalCredentialPort;

    @Mock
    private SaveLocalCredentialPort saveLocalCredentialPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @InjectMocks
    private RegisterLocalIdentityService service;

    @Test
    @DisplayName("회원가입은 이메일을 정규화하고 해시된 credential을 저장한다")
    void register_normalizes_email_and_saves_credential() {
        Account savedAccount = Account.create();
        UUID accountId = UUID.randomUUID();
        ReflectionTestUtils.setField(savedAccount, "id", accountId);

        given(loadLocalCredentialPort.loadByEmailNormalized("user@rallyon.local")).willReturn(Optional.empty());
        given(saveAccountPort.save(any())).willReturn(savedAccount);
        given(passwordHasherPort.hash("password123!")).willReturn("hashed-password");

        UUID result = service.register(new RegisterLocalIdentityCommand(" User@RallyOn.Local ", "password123!"));

        assertThat(result).isEqualTo(accountId);
        ArgumentCaptor<LocalCredential> credentialCaptor = ArgumentCaptor.forClass(LocalCredential.class);
        verify(saveLocalCredentialPort).save(credentialCaptor.capture());
        assertThat(credentialCaptor.getValue().getAccount()).isEqualTo(savedAccount);
        assertThat(credentialCaptor.getValue().getEmailNormalized()).isEqualTo("user@rallyon.local");
        assertThat(credentialCaptor.getValue().getPasswordHash()).isEqualTo("hashed-password");
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 ConflictException이 발생한다")
    void register_throws_when_email_already_exists() {
        given(loadLocalCredentialPort.loadByEmailNormalized("user@rallyon.local"))
                .willReturn(Optional.of(org.mockito.Mockito.mock(LocalCredential.class)));

        assertThatThrownBy(() -> service.register(new RegisterLocalIdentityCommand("user@rallyon.local", "password123!")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("이미 가입된 이메일입니다.");

        verify(saveAccountPort, never()).save(any());
    }

    @Test
    @DisplayName("비밀번호 정책을 만족하지 않으면 저장하지 않는다")
    void register_throws_when_password_is_invalid() {
        assertThatThrownBy(() -> service.register(new RegisterLocalIdentityCommand("user@rallyon.local", "short")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호는 8자 이상이어야 합니다.");

        verify(saveAccountPort, never()).save(any());
        verify(saveLocalCredentialPort, never()).save(any());
    }
}
