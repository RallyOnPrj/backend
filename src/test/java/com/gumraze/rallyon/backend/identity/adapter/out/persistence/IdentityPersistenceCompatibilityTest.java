package com.gumraze.rallyon.backend.identity.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityLocalCredentialRepository;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityOAuthLinkRepository;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.entity.IdentityLocalCredential;
import com.gumraze.rallyon.backend.identity.entity.IdentityOAuthLink;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private IdentityOAuthLinkRepository identityOAuthLinkRepository;

    @Autowired
    private IdentityLocalCredentialRepository identityLocalCredentialRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("GOOGLE identity_oauth_links row를 저장하고 조회할 수 있다")
    void loads_google_oauth_link_rows() {
        User user = userRepository.save(User.builder().build());

        identityOAuthLinkRepository.save(IdentityOAuthLink.builder()
                .user(user)
                .provider(AuthProvider.GOOGLE)
                .providerUserId("google-user-1")
                .email("google-user-1@rallyon.test")
                .nickname("google-user-1")
                .build());

        entityManager.flush();
        entityManager.clear();

        IdentityOAuthLink loaded = identityOAuthLinkRepository
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-user-1")
                .orElseThrow();

        assertThat(loaded.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(loaded.getProviderUserId()).isEqualTo("google-user-1");
        assertThat(loaded.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("identity_local_credentials row를 저장하고 이메일로 조회할 수 있다")
    void loads_local_credentials_by_normalized_email() {
        User user = userRepository.save(User.builder().build());

        identityLocalCredentialRepository.save(IdentityLocalCredential.builder()
                .user(user)
                .emailNormalized("user@rallyon.test")
                .passwordHash("hashed-password")
                .build());

        entityManager.flush();
        entityManager.clear();

        IdentityLocalCredential loaded = identityLocalCredentialRepository
                .findByEmailNormalized("user@rallyon.test")
                .orElseThrow();

        assertThat(loaded.getUserId()).isEqualTo(user.getId());
        assertThat(loaded.getPasswordHash()).isEqualTo("hashed-password");
    }
}
