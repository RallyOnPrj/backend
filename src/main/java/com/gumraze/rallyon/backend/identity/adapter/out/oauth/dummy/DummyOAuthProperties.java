package com.gumraze.rallyon.backend.identity.adapter.out.oauth.dummy;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.dummy")
public record DummyOAuthProperties(
        boolean enabled,
        boolean loginPageVisible
) {
}
