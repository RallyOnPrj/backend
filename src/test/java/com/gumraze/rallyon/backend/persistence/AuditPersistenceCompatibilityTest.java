package com.gumraze.rallyon.backend.persistence;

import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameManager;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameRepository;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.IdentityLocalCredentialRepository;
import com.gumraze.rallyon.backend.identity.entity.IdentityLocalCredential;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import com.gumraze.rallyon.backend.user.repository.UserProfileRepository;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
@Transactional
class AuditPersistenceCompatibilityTest {

    @MockitoBean
    private RestClient.Builder restClientBuilder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private IdentityLocalCredentialRepository identityLocalCredentialRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("UserProfile 저장/수정 시 audit timestamp가 자동으로 관리된다")
    void userProfile_managesAuditTimestampsAutomatically() {
        User user = userRepository.save(User.builder().build());
        UserProfile profile = userProfileRepository.save(UserProfile.builder()
                .user(user)
                .nickname("initial")
                .tag("TAG1")
                .tagChangedAt(LocalDateTime.now())
                .build());

        entityManager.flush();
        entityManager.clear();

        UserProfile persisted = userProfileRepository.findById(user.getId()).orElseThrow();
        LocalDateTime createdAt = persisted.getCreatedAt();
        LocalDateTime updatedAt = persisted.getUpdatedAt();

        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();

        persisted.setNickname("updated");
        entityManager.flush();
        entityManager.clear();

        UserProfile updatedProfile = userProfileRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedProfile.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updatedProfile.getUpdatedAt()).isAfterOrEqualTo(updatedAt);
    }

    @Test
    @DisplayName("IdentityLocalCredential 저장/수정 시 audit timestamp가 자동으로 관리된다")
    void identityLocalCredential_managesAuditTimestampsAutomatically() {
        User user = userRepository.save(User.builder().build());
        IdentityLocalCredential credential = identityLocalCredentialRepository.save(IdentityLocalCredential.builder()
                .user(user)
                .emailNormalized("audit@rallyon.local")
                .passwordHash("hashed-password")
                .build());

        entityManager.flush();
        entityManager.clear();

        IdentityLocalCredential persisted = identityLocalCredentialRepository.findById(user.getId()).orElseThrow();
        LocalDateTime createdAt = persisted.getCreatedAt();
        LocalDateTime updatedAt = persisted.getUpdatedAt();

        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();

        persisted.setPasswordHash("updated-hash");
        entityManager.flush();
        entityManager.clear();

        IdentityLocalCredential updated = identityLocalCredentialRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updatedAt);
    }

    @Test
    @DisplayName("GameManager 저장 시 createdAt이 자동으로 세팅된다")
    void gameManager_setsCreatedAtAutomatically() throws Exception {
        User organizer = userRepository.save(User.builder().build());
        User managerUser = userRepository.save(User.builder().build());
        FreeGame freeGame = gameRepository.save(FreeGame.builder()
                .title("audit-game")
                .organizer(organizer)
                .gradeType(GradeType.NATIONAL)
                .matchRecordMode(MatchRecordMode.RESULT)
                .build());

        GameManager gameManager = newGameManager();
        ReflectionTestUtils.setField(gameManager, "freeGame", freeGame);
        ReflectionTestUtils.setField(gameManager, "user", managerUser);

        entityManager.persist(gameManager);
        entityManager.flush();

        assertThat(ReflectionTestUtils.getField(gameManager, "createdAt")).isNotNull();
    }

    private GameManager newGameManager() throws Exception {
        Constructor<GameManager> constructor = GameManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
