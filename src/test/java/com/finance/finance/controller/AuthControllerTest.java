package com.finance.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.finance.dto.LoginRequest;
import com.finance.finance.dto.RegisterRequest;
import com.finance.finance.entity.User;
import com.finance.finance.repository.UserRepository;
import com.finance.finance.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ContextConfiguration(classes = {com.finance.finance.config.TestConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser();
        testUser.setPassword(passwordEncoder.encode("password"));
    }

    @Test
    void register_ValidRequest_ShouldReturnCreated() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    void register_UsernameAlreadyExists_ShouldReturnBadRequest() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_EmailAlreadyExists_ShouldReturnBadRequest() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ValidCredentials_ShouldReturnOk() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_UserNotFound_ShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
