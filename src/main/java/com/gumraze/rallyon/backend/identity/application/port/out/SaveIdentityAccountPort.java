package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.entity.IdentityAccount;

public interface SaveIdentityAccountPort {

    IdentityAccount save(IdentityAccount identityAccount);
}
