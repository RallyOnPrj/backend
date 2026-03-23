package com.gumraze.rallyon.backend.identity.authorizationserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth")
public class AuthorizationServerProperties {

    private String issuer = "http://localhost:8080";
    private String frontendBaseUrl = "http://localhost:3000";
    private BrowserClient browserClient = new BrowserClient();
    private Cookies cookies = new Cookies();
    private Tokens tokens = new Tokens();

    @Getter
    @Setter
    public static class BrowserClient {
        private String clientId = "rallyon-web";
        private String redirectUri = "http://localhost:8080/identity/session/callback";
        private List<String> scopes = List.of("openid", "profile", "email");
    }

    @Getter
    @Setter
    public static class Cookies {
        private String accessTokenName = "access_token";
        private String accessTokenDomain;
        private String accessTokenPath = "/";
        private String refreshTokenName = "refresh_token";
        private String refreshTokenDomain;
        private String refreshTokenPath = "/identity";
        private String sameSite = "Lax";
        private boolean secure = false;
    }

    @Getter
    @Setter
    public static class Tokens {
        private long accessTokenExpirationSeconds = 3600;
        private long refreshTokenExpirationSeconds = 43200;
        private long authorizationCodeExpirationSeconds = 300;
    }
}
