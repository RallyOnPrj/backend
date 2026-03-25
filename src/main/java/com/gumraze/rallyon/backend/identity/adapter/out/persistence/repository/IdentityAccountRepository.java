package com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository;

import com.gumraze.rallyon.backend.identity.entity.IdentityAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IdentityAccountRepository extends JpaRepository<IdentityAccount, UUID> {
}
