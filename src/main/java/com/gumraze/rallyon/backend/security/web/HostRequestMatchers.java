package com.gumraze.rallyon.backend.security.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class HostRequestMatchers {

    private static final Set<String> LOCAL_DEVELOPMENT_HOSTS = Set.of("localhost", "127.0.0.1");

    private HostRequestMatchers() {
    }

    public static RequestMatcher forConfiguredHost(String host) {
        return forHosts(host, "localhost", "127.0.0.1");
    }

    public static RequestMatcher forHosts(String... hosts) {
        Set<String> allowedHosts = Arrays.stream(hosts)
                .map(HostRequestMatchers::normalizeHost)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());

        return request -> {
            String requestHost = extractHost(request);
            return requestHost != null && allowedHosts.contains(requestHost);
        };
    }

    public static String extractHost(HttpServletRequest request) {
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && !forwardedHost.isBlank()) {
            return normalizeHost(forwardedHost);
        }

        String hostHeader = request.getHeader("Host");
        if (hostHeader != null && !hostHeader.isBlank()) {
            return normalizeHost(hostHeader);
        }

        return normalizeHost(request.getServerName());
    }

    public static boolean isLocalDevelopmentHost(String host) {
        String normalizedHost = normalizeHost(host);
        return normalizedHost != null && LOCAL_DEVELOPMENT_HOSTS.contains(normalizedHost);
    }

    private static String normalizeHost(String host) {
        if (host == null || host.isBlank()) {
            return null;
        }

        String normalized = host.trim().toLowerCase(Locale.ROOT);
        int commaIndex = normalized.indexOf(',');
        if (commaIndex >= 0) {
            normalized = normalized.substring(0, commaIndex).trim();
        }

        if (normalized.startsWith("http://")) {
            normalized = normalized.substring("http://".length());
        } else if (normalized.startsWith("https://")) {
            normalized = normalized.substring("https://".length());
        }

        int slashIndex = normalized.indexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(0, slashIndex);
        }

        if (normalized.startsWith("[")) {
            int endBracketIndex = normalized.indexOf(']');
            return endBracketIndex >= 0 ? normalized.substring(1, endBracketIndex) : normalized;
        }

        int colonIndex = normalized.indexOf(':');
        return colonIndex >= 0 ? normalized.substring(0, colonIndex) : normalized;
    }
}
