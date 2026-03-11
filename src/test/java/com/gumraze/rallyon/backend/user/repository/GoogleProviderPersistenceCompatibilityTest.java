package com.gumraze.rallyon.backend.user.repository;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;
import com.gumraze.rallyon.backend.user.entity.OauthUser;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.entity.UserAuth;
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
class GoogleProviderPersistenceCompatibilityTest {

    @MockitoBean
    private RestClient.Builder restClientBuilder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JpaUserAuthRepository userAuthRepository;

    @Autowired
    private OauthUserRepository oauthUserRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("기존 GOOGLE user_auth row를 조회할 수 있다")
    void loads_google_user_auth_rows() {
        User user = userRepository.save(User.builder().build());
        userAuthRepository.save(UserAuth.builder()
                .user(user)
                .provider(AuthProvider.GOOGLE)
                .providerUserId("google-user-1")
                .email("google-user-1@rallyon.test")
                .nickname("google-user-1")
                .build());

        entityManager.flush();
        entityManager.clear();

        UserAuth loaded = userAuthRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-user-1")
                .orElseThrow();

        assertThat(loaded.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(loaded.getProviderUserId()).isEqualTo("google-user-1");
    }

    @Test
    @DisplayName("기존 GOOGLE oauth_users row를 조회할 수 있다")
    void loads_google_oauth_user_rows() {
        oauthUserRepository.save(OauthUser.builder()
                .oauthProvider(AuthProvider.GOOGLE)
                .oauthId("google-oauth-1")
                .email("google-oauth-1@rallyon.test")
                .nickname("google-oauth-1")
                .profileImageUrl("https://example.com/profile.png")
                .build());

        entityManager.flush();
        entityManager.clear();

        OauthUser loaded = oauthUserRepository.findByOauthProviderAndOauthId(AuthProvider.GOOGLE, "google-oauth-1")
                .orElseThrow();

        assertThat(loaded.getOauthProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(loaded.getOauthId()).isEqualTo("google-oauth-1");
    }
}
