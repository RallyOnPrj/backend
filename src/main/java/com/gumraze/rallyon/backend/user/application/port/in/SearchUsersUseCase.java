package com.gumraze.rallyon.backend.user.application.port.in;

import com.gumraze.rallyon.backend.user.application.port.in.query.SearchUsersQuery;
import com.gumraze.rallyon.backend.user.dto.UserSearchResponse;
import org.springframework.data.domain.Page;

public interface SearchUsersUseCase {

    Page<UserSearchResponse> search(SearchUsersQuery query);
}
