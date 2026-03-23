package com.gumraze.rallyon.backend.identity.authentication.adapter.out.oauth;

import com.gumraze.rallyon.backend.identity.adapter.out.oauth.google.GoogleOAuthProperties;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.kakao.KakaoOAuthProperties;
import com.gumraze.rallyon.backend.identity.domain.authentication.AuthProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuthAuthorizationUrlFactory {

    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final GoogleOAuthProperties googleOAuthProperties;

    public OAuthAuthorizationUrlFactory(
            KakaoOAuthProperties kakaoOAuthProperties,
            GoogleOAuthProperties googleOAuthProperties
    ) {
        this.kakaoOAuthProperties = kakaoOAuthProperties;
        this.googleOAuthProperties = googleOAuthProperties;
    }

    public String create(AuthProvider provider, String redirectUri, String state) {
        return switch (provider) {
            case KAKAO -> UriComponentsBuilder.fromUriString(kakaoOAuthProperties.authorizationUri())
                    .queryParam("response_type", "code")
                    .queryParam("client_id", kakaoOAuthProperties.clientId())
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("scope", String.join(",", kakaoOAuthProperties.scopes()))
                    .queryParam("state", state)
                    .encode()
                    .toUriString();
            case GOOGLE -> UriComponentsBuilder.fromUriString(googleOAuthProperties.authorizationUri())
                    .queryParam("response_type", "code")
                    .queryParam("client_id", googleOAuthProperties.clientId())
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("scope", String.join(" ", googleOAuthProperties.scopes()))
                    .queryParam("state", state)
                    .encode()
                    .toUriString();
            default -> throw new IllegalArgumentException("지원되지 않는 provider: " + provider);
        };
    }
}
