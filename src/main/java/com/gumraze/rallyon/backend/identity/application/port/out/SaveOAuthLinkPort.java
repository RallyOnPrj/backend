package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.entity.OAuthLink;

public interface SaveOAuthLinkPort {

    OAuthLink save(OAuthLink oauthLink);
}
