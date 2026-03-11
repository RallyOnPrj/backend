package com.gumraze.rallyon.backend.user.repository;

import com.gumraze.rallyon.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
