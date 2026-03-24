package com.gumraze.rallyon.backend.identity.adapter.out.oauth.apple;

import com.gumraze.rallyon.backend.identity.adapter.out.oauth.apple.dto.AppleTokenResponse;
import com.gumraze.rallyon.backend.identity.application.port.out.OAuthProviderPort;
import com.gumraze.rallyon.backend.identity.domain.authentication.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.authentication.OAuthUserInfo;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
@ConditionalOnProperty(prefix = "oauth.apple", name = "enabled", havingValue = "true")
public class AppleOAuthClient implements OAuthProviderPort {

    private static final String APPLE_AUDIENCE = "https://appleid.apple.com";

    private final AppleOAuthProperties properties;
    private final RestClient restClient;

    public AppleOAuthClient(AppleOAuthProperties properties, RestClient.Builder restClientBuilder) {
        validateProperties(properties);
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public AuthProvider supports() {
        return AuthProvider.APPLE;
    }

    @Override
    public OAuthUserInfo getOAuthUserInfo(String authorizationCode, String redirectUri) {
        AppleTokenResponse tokenResponse = requestAccessToken(authorizationCode, redirectUri);
        JWTClaimsSet claims = parseIdTokenClaims(tokenResponse.idToken());

        String email = readStringClaim(claims, "email");
        String nickname = deriveNickname(email);

        return OAuthUserInfo.builder()
                .providerUserId(claims.getSubject())
                .email(email)
                .nickname(nickname)
                .emailVerified(parseBooleanClaim(claims, "email_verified"))
                .phoneNumberVerified(false)
                .build();
    }

    private AppleTokenResponse requestAccessToken(String authorizationCode, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", authorizationCode);
        form.add("redirect_uri", redirectUri);
        form.add("client_id", properties.clientId());
        form.add("client_secret", createClientSecret());

        return restClient.post()
                .uri(properties.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(AppleTokenResponse.class);
    }

    private String createClientSecret() {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .issuer(properties.teamId())
                    .subject(properties.clientId())
                    .audience(APPLE_AUDIENCE)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(300)))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(properties.keyId())
                            .build(),
                    claimsSet
            );
            signedJWT.sign(new RSASSASigner((RSAPrivateKey) parsePrivateKey(properties.privateKey())));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Apple client secret 생성에 실패했습니다.", e);
        }
    }

    private JWTClaimsSet parseIdTokenClaims(String idToken) {
        try {
            return SignedJWT.parse(idToken).getJWTClaimsSet();
        } catch (Exception e) {
            throw new IllegalStateException("Apple ID Token 파싱에 실패했습니다.", e);
        }
    }

    private PrivateKey parsePrivateKey(String privateKeyPem) {
        try {
            String normalized = privateKeyPem
                    .replace("\\n", "\n")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(normalized);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Apple private key 파싱에 실패했습니다.", e);
        }
    }

    private static Boolean parseBooleanClaim(JWTClaimsSet claims, String claimName) {
        Object value = claims.getClaim(claimName);
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        if (value instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }
        return null;
    }

    private static String readStringClaim(JWTClaimsSet claims, String claimName) {
        Object value = claims.getClaim(claimName);
        if (value instanceof String stringValue) {
            return stringValue;
        }
        return null;
    }

    private static String deriveNickname(String email) {
        if (email == null || email.isBlank()) {
            return "Apple User";
        }

        int delimiterIndex = email.indexOf('@');
        if (delimiterIndex <= 0) {
            return email;
        }
        return email.substring(0, delimiterIndex);
    }

    private static void validateProperties(AppleOAuthProperties properties) {
        Assert.hasText(properties.clientId(), "oauth.apple.client-id must not be blank when Apple OAuth is enabled.");
        Assert.hasText(properties.teamId(), "oauth.apple.team-id must not be blank when Apple OAuth is enabled.");
        Assert.hasText(properties.keyId(), "oauth.apple.key-id must not be blank when Apple OAuth is enabled.");
        Assert.hasText(properties.privateKey(), "oauth.apple.private-key must not be blank when Apple OAuth is enabled.");
        Assert.hasText(properties.authorizationUri(), "oauth.apple.authorization-uri must not be blank when Apple OAuth is enabled.");
        Assert.hasText(properties.tokenUri(), "oauth.apple.token-uri must not be blank when Apple OAuth is enabled.");
    }
}
