package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.entity.IdentityOAuthLink;

public interface SaveOAuthLinkPort {

    IdentityOAuthLink save(IdentityOAuthLink identityOAuthLink);
}
