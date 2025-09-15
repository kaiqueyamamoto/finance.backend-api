package com.finance.finance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.finance.dto.LoginRequest;
import com.finance.finance.dto.RegisterRequest;
import com.finance.finance.entity.User;
import com.finance.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerAndLogin_ShouldWork() throws Exception {
        // Register a new user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        // Verify user was created in database
        assertTrue(userRepository.findByUsername("testuser").isPresent());

        // Login with the created user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void register_DuplicateUsername_ShouldReturnBadRequest() throws Exception {
        // Create first user
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("password"));
        userRepository.save(existingUser);

        // Try to register with same username
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existinguser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Create user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        // Try to login with wrong password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
