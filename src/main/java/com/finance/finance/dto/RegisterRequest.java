package com.finance.finance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    private String username;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    private String email;

    @NotBlank(message = "Password é obrigatório")
    @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
    private String password;

    @NotBlank(message = "WhatsApp é obrigatório")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "WhatsApp deve ter um formato válido (ex: +5511999999999)")
    private String whatsapp;

    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    private String firstName;

    @Size(max = 50, message = "Sobrenome deve ter no máximo 50 caracteres")
    private String lastName;

    // Constructors
    public RegisterRequest() {}

    public RegisterRequest(String username, String email, String password, String whatsapp) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.whatsapp = whatsapp;
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
