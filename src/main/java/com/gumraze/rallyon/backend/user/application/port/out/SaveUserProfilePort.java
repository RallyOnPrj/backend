package com.gumraze.rallyon.backend.user.application.port.out;

import com.gumraze.rallyon.backend.user.entity.UserProfile;

public interface SaveUserProfilePort {

    UserProfile save(UserProfile userProfile);
}
