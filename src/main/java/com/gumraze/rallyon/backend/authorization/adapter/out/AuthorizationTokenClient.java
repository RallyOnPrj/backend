package com.gumraze.rallyon.backend.authorization.adapter.out;

import com.gumraze.rallyon.backend.authorization.config.AuthorizationProperties;
import com.gumraze.rallyon.backend.authorization.domain.BrowserAuthSession;
import com.gumraze.rallyon.backend.authorization.domain.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;

@Slf4j
@Component
public class AuthorizationTokenClient {

    private final RestClient restClient;
    private final AuthorizationProperties properties;

    @Autowired
    public AuthorizationTokenClient(
            RestClient.Builder restClientBuilder,
            AuthorizationProperties properties
    ) {
        this(
                buildRestClient(restClientBuilder),
                properties
        );
    }

    AuthorizationTokenClient(RestClient restClient, AuthorizationProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public TokenResponse exchangeAuthorizationCode(String code, BrowserAuthSession context) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.getBrowserClient().getClientId());
        form.add("redirect_uri", properties.getBrowserClient().getRedirectUri());
        form.add("code", code);
        form.add("code_verifier", context.codeVerifier());

        return postToTokenEndpoint(form);
    }

    public TokenResponse refresh(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", properties.getBrowserClient().getClientId());
        form.add("refresh_token", refreshToken);

        return postToTokenEndpoint(form);
    }

    private TokenResponse postToTokenEndpoint(MultiValueMap<String, String> form) {
        String tokenEndpointUrl = resolveTokenEndpointUrl();
        URI issuerUri = URI.create(properties.getIssuer());
        try {
            return restClient.post()
                .uri(tokenEndpointUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> applyForwardedHeaders(headers, issuerUri))
                .body(form)
                .retrieve()
                .body(TokenResponse.class);
        } catch (RestClientException ex) {
            log.warn(
                    "Token endpoint request failed. tokenEndpointUrl={}, internalBaseUrl={}, issuer={}",
                    tokenEndpointUrl,
                    properties.getInternalBaseUrl(),
                    properties.getIssuer(),
                    ex
            );
            throw ex;
        }
    }

    private String resolveTokenEndpointUrl() {
        String baseUrl = StringUtils.hasText(properties.getInternalBaseUrl())
                ? properties.getInternalBaseUrl()
                : properties.getIssuer();
        return baseUrl + "/oauth2/token";
    }

    private void applyForwardedHeaders(HttpHeaders headers, URI issuerUri) {
        headers.set(HttpHeaders.HOST, issuerUri.getHost());
        headers.set("X-Forwarded-Host", issuerUri.getHost());
        headers.set("X-Forwarded-Proto", issuerUri.getScheme());
        int port = issuerUri.getPort();
        if (port > 0) {
            headers.set("X-Forwarded-Port", Integer.toString(port));
        }
    }

    private static RestClient buildRestClient(RestClient.Builder restClientBuilder) {
        if (restClientBuilder == null) {
            return RestClient.builder()
                    .requestFactory(new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()))
                    .build();
        }

        RestClient.Builder configuredBuilder = restClientBuilder.requestFactory(
                new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault())
        );
        if (configuredBuilder == null) {
            configuredBuilder = restClientBuilder;
        }

        RestClient restClient = configuredBuilder.build();
        if (restClient == null) {
            return RestClient.builder()
                    .requestFactory(new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()))
                    .build();
        }

        return restClient;
    }
}
