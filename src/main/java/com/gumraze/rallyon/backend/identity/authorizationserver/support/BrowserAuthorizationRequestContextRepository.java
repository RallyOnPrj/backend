package com.gumraze.rallyon.backend.identity.authorizationserver.support;

import com.gumraze.rallyon.backend.identity.authorizationserver.domain.BrowserAuthorizationRequestContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BrowserAuthorizationRequestContextRepository {

    private static final String SESSION_KEY = "identity.authorization.request";

    public void save(HttpSession session, BrowserAuthorizationRequestContext context) {
        session.setAttribute(SESSION_KEY, context);
    }

    public Optional<BrowserAuthorizationRequestContext> load(HttpSession session) {
        return Optional.ofNullable(session.getAttribute(SESSION_KEY))
                .filter(BrowserAuthorizationRequestContext.class::isInstance)
                .map(BrowserAuthorizationRequestContext.class::cast);
    }

    public void clear(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
    }
}
