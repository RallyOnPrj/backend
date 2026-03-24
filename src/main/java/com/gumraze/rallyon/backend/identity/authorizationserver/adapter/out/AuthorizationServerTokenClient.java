package com.gumraze.rallyon.backend.identity.authorizationserver.adapter.out;

import com.gumraze.rallyon.backend.identity.authorizationserver.config.AuthorizationServerProperties;
import com.gumraze.rallyon.backend.identity.authorizationserver.domain.BrowserAuthorizationRequestContext;
import com.gumraze.rallyon.backend.identity.authorizationserver.domain.OAuthTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class AuthorizationServerTokenClient {

    private final RestClient restClient;
    private final AuthorizationServerProperties properties;

    public AuthorizationServerTokenClient(
            RestClient.Builder restClientBuilder,
            AuthorizationServerProperties properties
    ) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
    }

    public OAuthTokenResponse exchangeAuthorizationCode(String code, BrowserAuthorizationRequestContext context) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.getBrowserClient().getClientId());
        form.add("redirect_uri", properties.getBrowserClient().getRedirectUri());
        form.add("code", code);
        form.add("code_verifier", context.codeVerifier());

        return postToTokenEndpoint(form);
    }

    public OAuthTokenResponse refresh(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", properties.getBrowserClient().getClientId());
        form.add("refresh_token", refreshToken);

        return postToTokenEndpoint(form);
    }

    private OAuthTokenResponse postToTokenEndpoint(MultiValueMap<String, String> form) {
        return restClient.post()
                .uri(properties.getIssuer() + "/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(OAuthTokenResponse.class);
    }
}
