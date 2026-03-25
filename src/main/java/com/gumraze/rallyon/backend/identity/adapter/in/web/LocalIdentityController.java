package com.gumraze.rallyon.backend.identity.adapter.in.web;

import com.gumraze.rallyon.backend.identity.application.port.in.RegisterLocalIdentityUseCase;
import com.gumraze.rallyon.backend.identity.application.port.in.command.RegisterLocalIdentityCommand;
import com.gumraze.rallyon.backend.identity.dto.RegisterLocalIdentityRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocalIdentityController {

    private final RegisterLocalIdentityUseCase registerLocalIdentityUseCase;

    public LocalIdentityController(RegisterLocalIdentityUseCase registerLocalIdentityUseCase) {
        this.registerLocalIdentityUseCase = registerLocalIdentityUseCase;
    }

    @PostMapping(path = "/identity/accounts/local", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createLocalAccount(@Valid @RequestBody RegisterLocalIdentityRequest request) {
        registerLocalIdentityUseCase.register(new RegisterLocalIdentityCommand(request.getEmail(), request.getPassword()));
        return ResponseEntity.noContent().build();
    }
}
