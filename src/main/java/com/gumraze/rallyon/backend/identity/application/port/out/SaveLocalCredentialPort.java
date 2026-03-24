package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.entity.IdentityLocalCredential;

public interface SaveLocalCredentialPort {

    IdentityLocalCredential save(IdentityLocalCredential credential);
}
