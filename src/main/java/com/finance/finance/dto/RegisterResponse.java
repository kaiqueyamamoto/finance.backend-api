package com.finance.finance.dto;

import java.time.LocalDateTime;

public class RegisterResponse {

    private Long id;
    private String username;
    private String email;
    private String whatsapp;
    private String firstName;
    private String lastName;
    private String roles;
    private Boolean enabled;
    private LocalDateTime createdAt;

    // Constructors
    public RegisterResponse() {}

    public RegisterResponse(Long id, String username, String email, String whatsapp, 
                          String firstName, String lastName, String roles, 
                          Boolean enabled, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.whatsapp = whatsapp;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
