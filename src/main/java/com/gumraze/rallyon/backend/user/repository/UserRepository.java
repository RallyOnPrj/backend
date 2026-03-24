package com.gumraze.rallyon.backend.user.repository;

import com.gumraze.rallyon.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
