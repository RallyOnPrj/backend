package com.gumraze.rallyon.backend.identity.application.port.in;

import com.gumraze.rallyon.backend.identity.domain.AuthenticatedAccount;

public interface AuthenticateLocalIdentityUseCase {

    AuthenticatedAccount authenticate(String email, String password);
}
