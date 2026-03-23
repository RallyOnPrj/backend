package com.gumraze.rallyon.backend.identity.adapter.out.oauth;

import com.gumraze.rallyon.backend.identity.application.port.out.OAuthProviderPort;
import com.gumraze.rallyon.backend.identity.domain.authentication.AuthProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class OAuthProviderRegistry {

    private final Map<AuthProvider, OAuthProviderPort> providers = new EnumMap<>(AuthProvider.class);

    public OAuthProviderRegistry(List<OAuthProviderPort> providerBeans) {
        for (OAuthProviderPort providerBean : providerBeans) {
            providers.put(providerBean.supports(), providerBean);
        }
    }

    public OAuthProviderPort resolve(AuthProvider provider) {
        OAuthProviderPort resolved = providers.get(provider);
        if (resolved == null) {
            throw new IllegalArgumentException("지원되지 않는 OAuth 클라이언트: " + provider);
        }
        return resolved;
    }
}
