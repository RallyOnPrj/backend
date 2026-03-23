package com.gumraze.rallyon.backend.identity.authorizationserver.config;

import com.gumraze.rallyon.backend.security.web.HostRequestMatchers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import java.net.URI;

@Configuration
public class AuthOriginSecurityConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain authOriginSecurityFilterChain(
            HttpSecurity http,
            AuthorizationServerProperties properties,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        RequestMatcher authHostMatcher = HostRequestMatchers.forConfiguredHost(extractHost(properties.getIssuer()));
        RequestMatcher authEntryPoints = request -> {
            String path = request.getRequestURI();
            return "/login".equals(path)
                    || "/error".equals(path)
                    || path.startsWith("/identity/");
        };

        http
                .securityMatcher(new AndRequestMatcher(authHostMatcher, authEntryPoints))
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/identity/login/context",
                                "/identity/session/**",
                                "/identity/social/**",
                                "/identity/local/login",
                                "/identity/password/register",
                                "/identity/token/refresh",
                                "/identity/logout",
                                "/error"
                        ).permitAll()
                        .anyRequest().denyAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }

    private String extractHost(String url) {
        return URI.create(url).getHost();
    }
}
