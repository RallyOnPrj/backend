package com.gumraze.rallyon.backend.authorization.adapter.in.web;

import com.gumraze.rallyon.backend.identity.domain.AuthenticatedAccount;
import com.gumraze.rallyon.backend.identity.domain.AccountRole;
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
public class AuthenticatedAccountContextService {

    private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public void save(
            AuthenticatedAccount authenticatedAccount,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authenticatedAccount,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + authenticatedAccount.role().name()))
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);
    }

    public AuthenticatedAccount resolve(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if (authentication.getPrincipal() instanceof AuthenticatedAccount authenticatedAccount) {
            return authenticatedAccount;
        }

        if (authentication.getCredentials() instanceof Jwt jwt) {
            AccountRole role = Optional.ofNullable(jwt.getClaimAsStringList("roles"))
                    .filter(list -> !list.isEmpty())
                    .map(list -> AccountRole.valueOf(list.getFirst()))
                    .orElse(AccountRole.USER);
            return new AuthenticatedAccount(
                    UUID.fromString(jwt.getSubject()),
                    role,
                    jwt.getClaimAsString("name")
            );
        }

        return null;
    }
}
