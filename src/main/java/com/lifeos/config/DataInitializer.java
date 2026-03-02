package com.lifeos.config;

import com.lifeos.model.Role;
import com.lifeos.model.User;
import com.lifeos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    // Spring inyecta aquí los valores de tu application.properties
    @Value("${app.setup.admin.username}")
    private String adminUsername;

    @Value("${app.setup.admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                
                User admin = new User();
                admin.setUsername(adminUsername);
                // Encriptamos la variable, no el texto fijo
                admin.setPassword(passwordEncoder.encode(adminPassword)); 
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
                System.out.println(" ADMIN CREADO DE FORMA SEGURA");
            }
        };
    }
}