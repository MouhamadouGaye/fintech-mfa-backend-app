package com.mgaye.banking_application.init;

import com.mgaye.banking_application.entity.Role;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername(admin.getFirstName() + admin.getLastName());
            admin.setEmail("admin@bank.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setRole(Role.ADMIN);
            admin.setIsActive(true);
            admin.setIsVerified(true);
            userRepository.save(admin);

            System.out.println("Default admin user created:");
            System.out.println("Username: admin");
            System.out.println("Password: Admin@123");
        }

        // Create sample customer user
        if (!userRepository.existsByUsername("john.doe")) {
            User customer = new User();
            customer.setUsername("john.doe");
            customer.setEmail("john.doe@email.com");
            customer.setPassword(passwordEncoder.encode("Customer@123"));
            customer.setFirstName("John");
            customer.setLastName("Doe");
            customer.setPhoneNumber("+1234567890");
            customer.setAddress("123 Main St, City, State");
            customer.setDateOfBirth(LocalDate.parse("1990-01-01"));
            customer.setRole(Role.CUSTOMER);
            customer.setIsActive(true);
            customer.setIsVerified(true);
            userRepository.save(customer);

            System.out.println("Sample customer user created:");
            System.out.println("Username: john.doe");
            System.out.println("Password: Customer@123");
        }
    }
}