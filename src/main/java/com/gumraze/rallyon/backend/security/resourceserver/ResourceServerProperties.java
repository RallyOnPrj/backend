package com.gumraze.rallyon.backend.security.resourceserver;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.resource-server")
public class ResourceServerProperties {

    private String host = "api.rallyon.test";
    private String accessTokenName = "access_token";
}
