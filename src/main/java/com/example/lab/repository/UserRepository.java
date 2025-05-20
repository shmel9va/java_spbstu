package com.example.lab.repository;

import com.example.lab.model.User;

public interface UserRepository {
    User findByUsername(String username);
    User save(User user);
}
