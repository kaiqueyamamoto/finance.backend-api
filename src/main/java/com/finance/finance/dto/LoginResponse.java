package com.finance.finance.dto;

import java.time.LocalDateTime;

public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String roles;
    private LocalDateTime expiresAt;

    // Constructors
    public LoginResponse() {}

    public LoginResponse(String token, String username, String email, String roles, LocalDateTime expiresAt) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
