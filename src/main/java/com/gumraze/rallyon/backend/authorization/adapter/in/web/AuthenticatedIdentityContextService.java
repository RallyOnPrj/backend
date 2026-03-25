package com.gumraze.rallyon.backend.authorization.adapter.in.web;

import com.gumraze.rallyon.backend.identity.domain.AuthenticatedIdentity;
import com.gumraze.rallyon.backend.user.constants.UserRole;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthenticatedIdentityContextService {

    private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public void save(
            AuthenticatedIdentity authenticatedIdentity,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authenticatedIdentity,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + authenticatedIdentity.role().name()))
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);
    }

    public AuthenticatedIdentity resolve(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if (authentication.getPrincipal() instanceof AuthenticatedIdentity authenticatedIdentity) {
            return authenticatedIdentity;
        }

        if (authentication.getCredentials() instanceof Jwt jwt) {
            UserRole role = Optional.ofNullable(jwt.getClaimAsStringList("roles"))
                    .filter(list -> !list.isEmpty())
                    .map(list -> UserRole.valueOf(list.getFirst()))
                    .orElse(UserRole.USER);
            UserStatus status = Optional.ofNullable(jwt.getClaimAsString("status"))
                    .map(UserStatus::valueOf)
                    .orElse(UserStatus.ACTIVE);
            return new AuthenticatedIdentity(
                    UUID.fromString(jwt.getSubject()),
                    role,
                    status,
                    jwt.getClaimAsString("name")
            );
        }

        return null;
    }
}
