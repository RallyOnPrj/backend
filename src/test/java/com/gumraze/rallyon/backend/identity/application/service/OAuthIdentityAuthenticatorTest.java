package com.gumraze.rallyon.backend.identity.application.service;

import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthProviderRegistry;
import com.gumraze.rallyon.backend.identity.application.port.out.LoadOAuthLinkPort;
import com.gumraze.rallyon.backend.identity.application.port.out.OAuthProviderPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveIdentityAccountPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveOAuthLinkPort;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedIdentity;
import com.gumraze.rallyon.backend.identity.domain.OAuthUserInfo;
import com.gumraze.rallyon.backend.identity.entity.IdentityAccount;
import com.gumraze.rallyon.backend.identity.entity.IdentityOAuthLink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OAuthIdentityAuthenticatorTest {

    @Mock
    private OAuthProviderRegistry oAuthProviderRegistry;

    @Mock
    private LoadOAuthLinkPort loadOAuthLinkPort;

    @Mock
    private SaveOAuthLinkPort saveOAuthLinkPort;

    @Mock
    private SaveIdentityAccountPort saveIdentityAccountPort;

    private OAuthIdentityAuthenticator service;

    @BeforeEach
    void setUp() {
        service = new OAuthIdentityAuthenticator(
                oAuthProviderRegistry,
                new OAuthAllowedProvidersProperties(List.of(AuthProvider.KAKAO)),
                loadOAuthLinkPort,
                saveOAuthLinkPort,
                saveIdentityAccountPort
        );
    }

    @Test
    @DisplayName("허용되지 않은 provider면 예외가 발생한다")
    void authenticate_throws_when_provider_is_not_allowed() {
        assertThatThrownBy(() -> service.authenticate(AuthProvider.GOOGLE, "code", "https://auth.rallyon.test/callback"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않는 provider");
    }

    @Test
    @DisplayName("기존 OAuth link가 있으면 snapshot을 갱신하고 재사용한다")
    void authenticate_reuses_existing_link() {
        IdentityAccount identityAccount = account(UUID.randomUUID());
        IdentityOAuthLink link = IdentityOAuthLink.link(identityAccount, AuthProvider.KAKAO, "provider-user-1");
        OAuthProviderPort providerPort = org.mockito.Mockito.mock(OAuthProviderPort.class);
        OAuthUserInfo userInfo = new OAuthUserInfo(
                "provider-user-1",
                "user@rallyon.local",
                "kakao-player",
                null,
                null,
                null,
                null,
                null,
                true,
                false
        );

        given(oAuthProviderRegistry.resolve(AuthProvider.KAKAO)).willReturn(providerPort);
        given(providerPort.getOAuthUserInfo("code", "https://auth.rallyon.test/callback")).willReturn(userInfo);
        given(loadOAuthLinkPort.loadByProviderAndProviderUserId(AuthProvider.KAKAO, "provider-user-1")).willReturn(Optional.of(link));
        given(saveOAuthLinkPort.save(link)).willReturn(link);

        AuthenticatedIdentity result = service.authenticate(AuthProvider.KAKAO, "code", "https://auth.rallyon.test/callback");

        assertThat(result.identityAccountId()).isEqualTo(identityAccount.getId());
        assertThat(result.displayName()).isEqualTo("kakao-player");
        verify(saveIdentityAccountPort, never()).save(any());
    }

    @Test
    @DisplayName("OAuth link가 없으면 새 계정과 link를 생성한다")
    void authenticate_creates_new_account_and_link_when_missing() {
        UUID identityAccountId = UUID.randomUUID();
        IdentityAccount identityAccount = account(identityAccountId);
        OAuthProviderPort providerPort = org.mockito.Mockito.mock(OAuthProviderPort.class);
        OAuthUserInfo userInfo = new OAuthUserInfo(
                "provider-user-1",
                "user@rallyon.local",
                "kakao-player",
                null,
                null,
                null,
                null,
                null,
                true,
                false
        );

        given(oAuthProviderRegistry.resolve(AuthProvider.KAKAO)).willReturn(providerPort);
        given(providerPort.getOAuthUserInfo("code", "https://auth.rallyon.test/callback")).willReturn(userInfo);
        given(loadOAuthLinkPort.loadByProviderAndProviderUserId(AuthProvider.KAKAO, "provider-user-1")).willReturn(Optional.empty());
        given(saveIdentityAccountPort.save(any())).willReturn(identityAccount);
        given(saveOAuthLinkPort.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        AuthenticatedIdentity result = service.authenticate(AuthProvider.KAKAO, "code", "https://auth.rallyon.test/callback");

        assertThat(result.identityAccountId()).isEqualTo(identityAccountId);
        assertThat(result.displayName()).isEqualTo("kakao-player");

        ArgumentCaptor<IdentityOAuthLink> linkCaptor = ArgumentCaptor.forClass(IdentityOAuthLink.class);
        verify(saveOAuthLinkPort).save(linkCaptor.capture());
        assertThat(linkCaptor.getValue().getIdentityAccount()).isEqualTo(identityAccount);
        assertThat(linkCaptor.getValue().getProvider()).isEqualTo(AuthProvider.KAKAO);
        assertThat(linkCaptor.getValue().getProviderUserId()).isEqualTo("provider-user-1");
    }

    private IdentityAccount account(UUID identityAccountId) {
        IdentityAccount identityAccount = IdentityAccount.create();
        ReflectionTestUtils.setField(identityAccount, "id", identityAccountId);
        return identityAccount;
    }
}
