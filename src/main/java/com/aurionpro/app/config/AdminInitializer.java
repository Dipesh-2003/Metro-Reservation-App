package com.aurionpro.app.config;
import com.aurionpro.app.common.Role;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Optional<User> adminUser = userRepository.findByEmail("admin@metro.com");
        if (adminUser.isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@metro.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true); 
            userRepository.save(admin);
            System.out.println(">>>>>>>>>> Created default admin user <<<<<<<<<<");
        }
        
        Optional<User> staffUser = userRepository.findByEmail("staff@metro.com");
        if (staffUser.isEmpty()) {
            User staff = new User();
            staff.setName("Staff Member");
            staff.setEmail("staff@metro.com");
            staff.setPassword(passwordEncoder.encode("staff123")); //default password
            staff.setRole(Role.STAFF);
            staff.setEnabled(true); // Staff accounts enabled by default
            userRepository.save(staff);
            System.out.println(">>>>>>>>>> Created default staff user <<<<<<<<<<");
        }
    }
}