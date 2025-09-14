package com.finance.finance.config;

import com.finance.finance.entity.User;
import com.finance.finance.repository.UserRepository;
import com.finance.finance.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialize default categories if they don't exist
        categoryService.initializeDefaultCategories();
        
        // Initialize default user if it doesn't exist
        initializeDefaultUser();
    }

    private void initializeDefaultUser() {
        String username = "kaiqueyamamoto";
        String email = "kaiqueyamamoto@example.com";
        
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8="));
            user.setEnabled(true);
            user.setRoles("USER,ADMIN");
            
            userRepository.save(user);
            System.out.println("Usuário padrão 'kaiqueyamamoto' criado com sucesso!");
        } else {
            System.out.println("Usuário 'kaiqueyamamoto' já existe.");
        }
    }
}
