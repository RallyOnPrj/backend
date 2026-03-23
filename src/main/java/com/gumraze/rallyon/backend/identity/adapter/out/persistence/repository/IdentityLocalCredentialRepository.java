package com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository;

import com.gumraze.rallyon.backend.identity.entity.IdentityLocalCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdentityLocalCredentialRepository extends JpaRepository<IdentityLocalCredential, UUID> {

    Optional<IdentityLocalCredential> findByEmailNormalized(String emailNormalized);
}
