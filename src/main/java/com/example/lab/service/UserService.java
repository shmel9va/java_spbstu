package com.example.lab.service;

import com.example.lab.model.User;

public interface UserService {
    User getUserByUsername(String username);
    User createUser(User user);
    User login(String username, String password);
}
