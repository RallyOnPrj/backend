package com.gumraze.rallyon.backend.identity.application.port.in;

import com.gumraze.rallyon.backend.identity.domain.AuthenticatedIdentity;

public interface AuthenticateLocalIdentityUseCase {

    AuthenticatedIdentity authenticate(String email, String password);
}
