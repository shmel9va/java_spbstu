package com.example.lab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@Configuration
public class ProfileConfig {
    
    @Bean
    @Profile("database")
    public CommandLineRunner databaseProfileInfo() {
        return args -> {
            System.out.println("============================");
            System.out.println("Running with DATABASE (H2) profile");
            System.out.println("============================");
        };
    }
    
    @Bean
    @Profile("dev")
    public CommandLineRunner devProfileInfo() {
        return args -> {
            System.out.println("============================");
            System.out.println("Running with DEV (in-memory) profile");
            System.out.println("============================");
        };
    }

    @Bean
    @Profile("mysql")
    public CommandLineRunner mysqlProfileInfo() {
        return args -> {
            System.out.println("============================");
            System.out.println("Running with MySQL profile");
            System.out.println("============================");
        };
    }
}
