package com.finance.finance.controller;

import com.finance.finance.config.JwtConfig;
import com.finance.finance.dto.LoginRequest;
import com.finance.finance.dto.LoginResponse;
import com.finance.finance.entity.User;
import com.finance.finance.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints para autenticação e autorização")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private JwtConfig jwtConfig;

    @PostMapping("/login")
    @Operation(summary = "Fazer login", description = "Autentica o usuário e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Autenticar o usuário
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Buscar o usuário no banco de dados
            Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Usuário não encontrado"));
            }

            User user = userOptional.get();

            // Gerar o token JWT
            String token = jwtConfig.generateToken(user.getUsername(), user.getRoles());

            // Calcular a data de expiração
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtConfig.getExpiration() / 1000);

            // Criar a resposta
            LoginResponse loginResponse = new LoginResponse(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles(),
                    expiresAt
            );

            return ResponseEntity.ok(loginResponse);

        } catch (AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Credenciais inválidas");
            error.put("message", "Username ou password incorretos");
            return ResponseEntity.status(401).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno do servidor");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Fazer logout", description = "Invalida a sessão do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso")
    })
    public ResponseEntity<Map<String, String>> logout() {
        SecurityContextHolder.clearContext();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout realizado com sucesso");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Informações do usuário atual", description = "Retorna as informações do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações obtidas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Não autorizado"));
            }

            String username = authentication.getName();
            Optional<User> userOptional = userRepository.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Usuário não encontrado"));
            }

            User user = userOptional.get();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("roles", user.getRoles());
            userInfo.put("enabled", user.getEnabled());
            userInfo.put("createdAt", user.getCreatedAt());

            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno do servidor");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar token", description = "Valida se o token JWT é válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    public ResponseEntity<Map<String, Object>> validateToken(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated()) {
            response.put("valid", true);
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
            return ResponseEntity.ok(response);
        } else {
            response.put("valid", false);
            response.put("message", "Token inválido ou expirado");
            return ResponseEntity.status(401).body(response);
        }
    }
}
