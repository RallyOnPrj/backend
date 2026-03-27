package com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository;

import com.gumraze.rallyon.backend.identity.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
