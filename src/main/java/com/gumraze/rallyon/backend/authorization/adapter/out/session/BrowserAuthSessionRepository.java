package com.gumraze.rallyon.backend.authorization.adapter.out.session;

import com.gumraze.rallyon.backend.authorization.domain.BrowserAuthSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BrowserAuthSessionRepository {

    private static final String SESSION_KEY = "identity.authorization.request";

    public void save(HttpSession session, BrowserAuthSession context) {
        session.setAttribute(SESSION_KEY, context);
    }

    public Optional<BrowserAuthSession> load(HttpSession session) {
        return Optional.ofNullable(session.getAttribute(SESSION_KEY))
                .filter(BrowserAuthSession.class::isInstance)
                .map(BrowserAuthSession.class::cast);
    }

    public void clear(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
    }
}
