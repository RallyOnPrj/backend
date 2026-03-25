package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.user.application.port.in.SearchUsersUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.query.SearchUsersQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.dto.UserSearchResponse;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SearchUsersService implements SearchUsersUseCase {

    private final LoadUserProfilePort loadUserProfilePort;

    @Override
    public Page<UserSearchResponse> search(SearchUsersQuery query) {
        if (query.nickname() == null || query.nickname().isBlank()) {
            throw new IllegalArgumentException("닉네임이 없습니다.");
        }

        if (query.tag() == null || query.tag().isBlank()) {
            return loadUserProfilePort.loadByNicknameContaining(query.nickname(), query.pageable())
                    .map(this::toResponse);
        }

        String normalizedTag = query.tag().replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);

        UserSearchResponse found = loadUserProfilePort.loadByNicknameAndTag(query.nickname(), normalizedTag)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("유저가 없습니다."));

        return new PageImpl<>(List.of(found), query.pageable(), 1);
    }

    private UserSearchResponse toResponse(UserProfile userProfile) {
        return new UserSearchResponse(
                userProfile.getIdentityAccountId(),
                userProfile.getNickname(),
                userProfile.getTag(),
                userProfile.getProfileImageUrl()
        );
    }
}
