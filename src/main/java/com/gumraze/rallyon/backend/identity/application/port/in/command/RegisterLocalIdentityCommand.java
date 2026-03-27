package com.gumraze.rallyon.backend.identity.application.port.in.command;

public record RegisterLocalIdentityCommand(
        String email,
        String password
) {
}
