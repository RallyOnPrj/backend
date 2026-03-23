package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.user.entity.User;

public interface SaveIdentityAccountPort {

    User save(User user);
}
