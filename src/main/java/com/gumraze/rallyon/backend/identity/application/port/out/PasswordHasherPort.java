package com.gumraze.rallyon.backend.identity.application.port.out;

public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
