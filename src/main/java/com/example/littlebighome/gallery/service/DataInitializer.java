package com.example.littlebighome.gallery.service;

import com.example.littlebighome.gallery.entity.Category;
import com.example.littlebighome.gallery.entity.User;
import com.example.littlebighome.gallery.repository.CategoryRepository;
import com.example.littlebighome.gallery.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, CategoryRepository categoryRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Initialize Admin
        if (userRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Change on first login/production
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
        }

        // Initialize Default Categories
        if (categoryRepository.count() == 0) {
            Arrays.asList("Living Room", "Bedroom", "Kitchen", "Commercial")
                    .forEach(name -> {
                        Category cat = new Category();
                        cat.setName(name);
                        cat.setSlug(name.toLowerCase().replace(" ", "-"));
                        categoryRepository.save(cat);
                    });
        }
    }
}