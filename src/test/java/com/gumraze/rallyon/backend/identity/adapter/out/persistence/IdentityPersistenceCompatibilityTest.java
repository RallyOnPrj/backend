package com.gumraze.rallyon.backend.identity.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityAccountRepository;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityLocalCredentialRepository;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityOAuthLinkRepository;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.OAuthUserInfo;
import com.gumraze.rallyon.backend.identity.entity.IdentityAccount;
import com.gumraze.rallyon.backend.identity.entity.IdentityLocalCredential;
import com.gumraze.rallyon.backend.identity.entity.IdentityOAuthLink;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
@Transactional
class IdentityPersistenceCompatibilityTest {

    @MockitoBean
    private RestClient.Builder restClientBuilder;

    @Autowired
    private IdentityAccountRepository identityAccountRepository;

    @Autowired
    private IdentityOAuthLinkRepository identityOAuthLinkRepository;

    @Autowired
    private IdentityLocalCredentialRepository identityLocalCredentialRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("GOOGLE identity_oauth_links row를 저장하고 조회할 수 있다")
    void loads_google_oauth_link_rows() {
        IdentityAccount identityAccount = identityAccountRepository.save(IdentityAccount.create());
        IdentityOAuthLink link = IdentityOAuthLink.link(identityAccount, AuthProvider.GOOGLE, "google-user-1");
        link.applySnapshot(new OAuthUserInfo(
                "google-user-1",
                "google-user-1@rallyon.test",
                "google-user-1",
                null,
                null,
                null,
                null,
                null,
                true,
                false
        ));

        identityOAuthLinkRepository.save(link);

        entityManager.flush();
        entityManager.clear();

        IdentityOAuthLink loaded = identityOAuthLinkRepository
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-user-1")
                .orElseThrow();

        assertThat(loaded.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(loaded.getProviderUserId()).isEqualTo("google-user-1");
        assertThat(loaded.getIdentityAccount().getId()).isEqualTo(identityAccount.getId());
        assertThat(loaded.getNickname()).isEqualTo("google-user-1");
    }

    @Test
    @DisplayName("identity_local_credentials row를 저장하고 이메일로 조회할 수 있다")
    void loads_local_credentials_by_normalized_email() {
        IdentityAccount identityAccount = identityAccountRepository.save(IdentityAccount.create());

        identityLocalCredentialRepository.save(IdentityLocalCredential.issue(
                identityAccount,
                "user@rallyon.test",
                "hashed-password"
        ));

        entityManager.flush();
        entityManager.clear();

        IdentityLocalCredential loaded = identityLocalCredentialRepository
                .findByEmailNormalized("user@rallyon.test")
                .orElseThrow();

        assertThat(loaded.getIdentityAccountId()).isEqualTo(identityAccount.getId());
        assertThat(loaded.getPasswordHash()).isEqualTo("hashed-password");
    }
}
