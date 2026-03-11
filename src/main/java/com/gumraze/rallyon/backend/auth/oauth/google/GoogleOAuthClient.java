package com.gumraze.rallyon.backend.auth.oauth.google;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;
import com.gumraze.rallyon.backend.auth.oauth.OAuthClient;
import com.gumraze.rallyon.backend.auth.oauth.OAuthUserInfo;
import com.gumraze.rallyon.backend.auth.oauth.ProviderAwareOAuthClient;
import com.gumraze.rallyon.backend.auth.oauth.google.dto.GoogleTokenResponse;
import com.gumraze.rallyon.backend.auth.oauth.google.dto.GoogleUserResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(prefix = "oauth.google", name = "enabled", havingValue = "true")
public class GoogleOAuthClient implements OAuthClient, ProviderAwareOAuthClient {

    private final GoogleOAuthProperties properties;
    private final RestClient restClient;

    public GoogleOAuthClient(
            GoogleOAuthProperties properties,
            RestClient.Builder restClient) {
        validateProperties(properties);
        this.properties = properties;
        this.restClient = restClient.build();
    }


    /**
     * Exchange a Google authorization code for an access token, retrieve the corresponding Google user
     * information, and map it to an OAuthUserInfo instance.
     *
     * @param authorizationCode the authorization code received from Google's OAuth 2.0 flow
     * @param redirectUri       the redirect URI used in the OAuth 2.0 exchange
     * @return an OAuthUserInfo populated with the provider user id (sub), email, name, profile image(s),
     *         email verification status, and other provider-specific fields (unused fields are null or false)
     */
    @Override
    public OAuthUserInfo getOAuthUserInfo(String authorizationCode, String redirectUri) {
        // Authorization Code를 구글의 액세스 토큰으로 교환
        GoogleTokenResponse tokenResponse = requestAccessToken(authorizationCode, redirectUri);

        // 구글 액세스 토큰으로 구글에 저장된 사용자 정보 호출
        GoogleUserResponse user = requestUserInfo(tokenResponse.accessToken());

        // providerUserId로 sub 변환
        return new OAuthUserInfo(
                user.sub(),
                user.email(),
                user.name(),
                user.picture(),
                user.picture(),
                null,
                null,
                null,
                user.emailVerified() != null && user.emailVerified(),
                false
        );
    }

    /**
     * Exchange an OAuth authorization code for Google's token response.
     *
     * @param authorizationCode the authorization code received from Google's authorization endpoint
     * @param redirectUri       the redirect URI that was used in the authorization request
     * @return                  a GoogleTokenResponse containing the access token and related token fields
     */
    private GoogleTokenResponse requestAccessToken(
            String authorizationCode,
            String redirectUri
    ) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", authorizationCode);
        form.add("redirect_uri", redirectUri);
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());

        return restClient.post()
                .uri(properties.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }

    private GoogleUserResponse requestUserInfo(String accessToken) {
        return restClient.get()
                .uri(properties.userInfoUri())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserResponse.class);
    }


    @Override
    public AuthProvider supports() {
        return AuthProvider.GOOGLE;
    }

    private static void validateProperties(GoogleOAuthProperties properties) {
        Assert.hasText(properties.clientId(), "oauth.google.client-id must not be blank when Google OAuth is enabled.");
        Assert.hasText(properties.clientSecret(), "oauth.google.client-secret must not be blank when Google OAuth is enabled.");
        Assert.hasText(properties.tokenUri(), "oauth.google.token-uri must not be blank when Google OAuth is enabled.");
        Assert.hasText(properties.userInfoUri(), "oauth.google.user-info-uri must not be blank when Google OAuth is enabled.");
    }
}
