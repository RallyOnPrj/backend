package com.gumraze.rallyon.backend.identity.authorizationserver.adapter.out;

import com.gumraze.rallyon.backend.identity.authorizationserver.config.AuthorizationServerProperties;
import com.gumraze.rallyon.backend.identity.authorizationserver.domain.BrowserAuthorizationRequestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AuthorizationServerTokenClientTest {

    @Test
    @DisplayName("인가 코드 교환은 internal base url을 사용한다")
    void exchangeAuthorizationCode_usesInternalBaseUrl() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();

        AuthorizationServerProperties properties = new AuthorizationServerProperties();
        properties.setIssuer("https://auth.rallyon.test");
        properties.setInternalBaseUrl("http://backend:8080");
        properties.getBrowserClient().setClientId("rallyon-web");
        properties.getBrowserClient().setRedirectUri("https://auth.rallyon.test/identity/session/callback");

        AuthorizationServerTokenClient client = new AuthorizationServerTokenClient(restClientBuilder.build(), properties);

        server.expect(requestTo("http://backend:8080/oauth2/token"))
                .andExpect(method(POST))
                .andExpect(header("Host", "auth.rallyon.test"))
                .andExpect(header("X-Forwarded-Host", "auth.rallyon.test"))
                .andExpect(header("X-Forwarded-Proto", "https"))
                .andRespond(withSuccess("""
                        {
                          "access_token": "access-token",
                          "refresh_token": "refresh-token",
                          "token_type": "Bearer",
                          "expires_in": 900,
                          "scope": "openid profile email"
                        }
                        """, MediaType.APPLICATION_JSON));

        client.exchangeAuthorizationCode(
                "authorization-code",
                new BrowserAuthorizationRequestContext("auth-state", "social-state", "code-verifier", "/court-manager", "login")
        );

        server.verify();
    }

    @Test
    @DisplayName("리프레시 토큰 재발급도 internal base url을 사용한다")
    void refresh_usesInternalBaseUrl() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();

        AuthorizationServerProperties properties = new AuthorizationServerProperties();
        properties.setIssuer("https://auth.rallyon.test");
        properties.setInternalBaseUrl("http://backend:8080");
        properties.getBrowserClient().setClientId("rallyon-web");

        AuthorizationServerTokenClient client = new AuthorizationServerTokenClient(restClientBuilder.build(), properties);

        server.expect(requestTo("http://backend:8080/oauth2/token"))
                .andExpect(method(POST))
                .andExpect(header("Host", "auth.rallyon.test"))
                .andExpect(header("X-Forwarded-Host", "auth.rallyon.test"))
                .andExpect(header("X-Forwarded-Proto", "https"))
                .andRespond(withSuccess("""
                        {
                          "access_token": "new-access-token",
                          "refresh_token": "new-refresh-token",
                          "token_type": "Bearer",
                          "expires_in": 900,
                          "scope": "openid profile email"
                        }
                        """, MediaType.APPLICATION_JSON));

        client.refresh("refresh-token");

        server.verify();
    }
}
