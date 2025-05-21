package com.example.lab.repository.impl;

import com.example.lab.model.User;
import com.example.lab.repository.UserRepository;
import com.example.lab.repository.jpa.JpaUserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("database")
public class JpaUserRepositoryAdapter implements UserRepository {
    
    private final JpaUserRepository jpaUserRepository;
    
    public JpaUserRepositoryAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }
    
    @Override
    public User findByUsername(String username) {
        return jpaUserRepository.findByUsername(username);
    }
    
    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }
}
