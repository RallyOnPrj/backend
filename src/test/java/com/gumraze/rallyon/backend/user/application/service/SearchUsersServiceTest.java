package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.user.application.port.in.query.SearchUsersQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.dto.UserSearchResponse;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.gumraze.rallyon.backend.support.UuidTestFixtures.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchUsersServiceTest {

    @Mock
    private LoadUserProfilePort loadUserProfilePort;

    @Test
    @DisplayName("nickname 포함 검색으로 사용자 검색")
    void search_by_nickname_containing() {
        var pageable = PageRequest.of(0, 20);
        var profile = UserProfile.create(
                uuid(1),
                "김대환",
                null,
                null,
                null,
                LocalDateTime.of(1998, 9, 25, 0, 0),
                null,
                "AB12",
                LocalDateTime.now()
        );

        SearchUsersService service = new SearchUsersService(loadUserProfilePort);
        when(loadUserProfilePort.loadByNicknameContaining("김대환", pageable))
                .thenReturn(new PageImpl<>(List.of(profile), pageable, 1));

        Page<UserSearchResponse> result = service.search(new SearchUsersQuery("김대환", null, pageable));

        verify(loadUserProfilePort).loadByNicknameContaining("김대환", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().identityAccountId()).isEqualTo(uuid(1));
        assertThat(result.getContent().getFirst().tag()).isEqualTo("AB12");
    }

    @Test
    @DisplayName("nickname과 tag로 검색 시 태그를 정규화한다")
    void search_by_nickname_and_tag_normalizes_tag() {
        var pageable = PageRequest.of(0, 20);
        var profile = UserProfile.create(
                uuid(1),
                "김대환",
                null,
                null,
                null,
                LocalDateTime.of(1998, 9, 25, 0, 0),
                null,
                "AB12",
                LocalDateTime.now()
        );

        SearchUsersService service = new SearchUsersService(loadUserProfilePort);
        when(loadUserProfilePort.loadByNicknameAndTag("김대환", "AB12"))
                .thenReturn(Optional.of(profile));

        Page<UserSearchResponse> result = service.search(new SearchUsersQuery("김대환", "ab-12", pageable));

        verify(loadUserProfilePort).loadByNicknameAndTag("김대환", "AB12");
        assertThat(result.getContent()).singleElement()
                .extracting(UserSearchResponse::tag)
                .isEqualTo("AB12");
    }
}
