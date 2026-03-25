package com.gumraze.rallyon.backend.identity.authorizationserver.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationServerConfigTest {

    @Test
    @DisplayName("인가 서버 JWK는 설정된 RSA private key와 고정 kid를 사용한다")
    void jwkSource_usesConfiguredPrivateKeyAndKeyId() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        AuthorizationServerProperties properties = new AuthorizationServerProperties();
        properties.getSigningKey().setPrivateKeyPem(toPrivateKeyPem(keyPair));
        properties.getSigningKey().setKeyId("test-auth-key");
        properties.getSigningKey().setAllowGeneratedKeyFallback(false);

        AuthorizationServerConfig config = new AuthorizationServerConfig();
        List<JWK> keys = config.jwkSource(properties)
                .get(new JWKSelector(new JWKMatcher.Builder().build()), (SecurityContext) null);

        assertThat(keys).hasSize(1);
        assertThat(keys.getFirst()).isInstanceOf(RSAKey.class);

        RSAKey rsaKey = (RSAKey) keys.getFirst();
        assertThat(rsaKey.getKeyID()).isEqualTo("test-auth-key");
        assertThat(rsaKey.toRSAPublicKey().getModulus())
                .isEqualTo(((RSAPublicKey) keyPair.getPublic()).getModulus());
    }

    private String toPrivateKeyPem(KeyPair keyPair) {
        String encoded = Base64.getMimeEncoder(64, "\n".getBytes())
                .encodeToString(keyPair.getPrivate().getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" + encoded + "\n-----END PRIVATE KEY-----";
    }
}
