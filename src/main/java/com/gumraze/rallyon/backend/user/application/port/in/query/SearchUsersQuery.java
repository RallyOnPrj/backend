package com.gumraze.rallyon.backend.user.application.port.in.query;

import org.springframework.data.domain.Pageable;

public record SearchUsersQuery(
        String nickname,
        String tag,
        Pageable pageable
) {
}
