package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyProfileDefaultsQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadAccountDisplayNamePort;
import com.gumraze.rallyon.backend.user.dto.UserProfileDefaultsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetMyProfileDefaultsServiceTest {

    @Mock
    private LoadAccountDisplayNamePort loadAccountDisplayNamePort;

    @InjectMocks
    private GetMyProfileDefaultsService service;

    @Test
    @DisplayName("display name이 있으면 suggestedNickname과 hasSuggestedNickname=true를 반환한다")
    void get_returns_suggested_nickname_when_display_name_exists() {
        UUID accountId = UUID.randomUUID();
        given(loadAccountDisplayNamePort.loadLatestDisplayName(accountId)).willReturn(Optional.of("kakao-player"));

        UserProfileDefaultsResponse result = service.get(new GetMyProfileDefaultsQuery(accountId));

        assertThat(result.suggestedNickname()).isEqualTo("kakao-player");
        assertThat(result.hasSuggestedNickname()).isTrue();
    }

    @Test
    @DisplayName("display name이 없으면 suggestedNickname=null과 hasSuggestedNickname=false를 반환한다")
    void get_returns_empty_defaults_when_display_name_does_not_exist() {
        UUID accountId = UUID.randomUUID();
        given(loadAccountDisplayNamePort.loadLatestDisplayName(accountId)).willReturn(Optional.empty());

        UserProfileDefaultsResponse result = service.get(new GetMyProfileDefaultsQuery(accountId));

        assertThat(result.suggestedNickname()).isNull();
        assertThat(result.hasSuggestedNickname()).isFalse();
    }
}
