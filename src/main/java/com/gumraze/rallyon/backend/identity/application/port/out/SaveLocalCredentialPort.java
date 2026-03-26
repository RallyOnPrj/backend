package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.entity.LocalCredential;

public interface SaveLocalCredentialPort {

    LocalCredential save(LocalCredential credential);
}
