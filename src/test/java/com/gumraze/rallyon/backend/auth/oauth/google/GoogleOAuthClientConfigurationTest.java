package com.gumraze.rallyon.backend.auth.oauth.google;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleOAuthClientConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(TestConfig.class);

    @Test
    void does_not_register_google_client_when_disabled() {
        contextRunner
                .withPropertyValues("oauth.google.enabled=false")
                .run(context -> assertThat(context.getBeansOfType(GoogleOAuthClient.class)).isEmpty());
    }

    @Test
    void fails_fast_when_google_enabled_without_credentials() {
        contextRunner
                .withPropertyValues(
                        "oauth.google.enabled=true",
                        "oauth.google.token-uri=https://example.com/token",
                        "oauth.google.user-info-uri=https://example.com/user"
                )
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNotNull();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("oauth.google.client-id must not be blank when Google OAuth is enabled.");
                });
    }

    @Test
    void registers_google_client_when_enabled_and_configured() {
        contextRunner
                .withPropertyValues(
                        "oauth.google.enabled=true",
                        "oauth.google.client-id=test-client-id",
                        "oauth.google.client-secret=test-client-secret",
                        "oauth.google.token-uri=https://example.com/token",
                        "oauth.google.user-info-uri=https://example.com/user"
                )
                .run(context -> assertThat(context.getBeansOfType(GoogleOAuthClient.class)).hasSize(1));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(GoogleOAuthProperties.class)
    @Import(GoogleOAuthClient.class)
    static class TestConfig {

        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }
    }
}
