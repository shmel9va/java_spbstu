package com.example.lab.repository.jpa;

import com.example.lab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}
