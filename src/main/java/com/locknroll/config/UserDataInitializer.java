package com.locknroll.config;

import com.locknroll.entity.Role;
import com.locknroll.entity.User;
import com.locknroll.repository.RoleRepository;
import com.locknroll.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Initialize default users and roles
 */
@Component
public class UserDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(UserDataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing default users and roles...");
        
        // Create roles if they don't exist
        createDefaultRoles();
        
        // Create default users if they don't exist
        createDefaultUsers();
        
        logger.info("User data initialization completed");
    }

    private void createDefaultRoles() {
        String[] roleNames = {"ADMIN", "BACKOFFICE", "SELLER", "MANAGER", "FINANCE", "QUALITY"};
        
        for (String roleName : roleNames) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                role.setDescription("Default " + roleName + " role");
                role.setIsActive(true);
                role.setCreatedBy("system");
                
                roleRepository.save(role);
                logger.info("Created role: {}", roleName);
            }
        }
    }

    private void createDefaultUsers() {
        // Admin user
        createUserIfNotExists("admin", "admin@locknroll.com", "password123", "Admin", "User", 
                            "1234567890", "ADMIN");
        
        // BackOffice user
        createUserIfNotExists("backoffice", "backoffice@locknroll.com", "password123", "BackOffice", "User", 
                            "1234567891", "BACKOFFICE");
        
        // Seller users
        createUserIfNotExists("seller1", "seller1@locknroll.com", "password123", "John", "Seller", 
                            "1234567892", "SELLER");
        createUserIfNotExists("seller2", "seller2@locknroll.com", "password123", "Jane", "Seller", 
                            "1234567893", "SELLER");
        
        // Manager user
        createUserIfNotExists("manager1", "manager1@locknroll.com", "password123", "Manager", "User", 
                            "1234567894", "MANAGER");
        
        // Finance user
        createUserIfNotExists("finance1", "finance1@locknroll.com", "password123", "Finance", "User", 
                            "1234567895", "FINANCE");
        
        // Quality user
        createUserIfNotExists("quality1", "quality1@locknroll.com", "password123", "Quality", "User", 
                            "1234567896", "QUALITY");
    }

    private void createUserIfNotExists(String username, String email, String password, 
                                     String firstName, String lastName, String phoneNumber, String roleName) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhoneNumber(phoneNumber);
            user.setIsActive(true);
            user.setCreatedBy("system");
            
            // Set role
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            user.setRoles(new HashSet<>(Arrays.asList(role)));
            
            userRepository.save(user);
            logger.info("Created user: {} with role: {}", username, roleName);
        }
    }
}