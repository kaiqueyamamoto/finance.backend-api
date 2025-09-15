package com.finance.finance.controller;

import com.finance.finance.config.JwtConfig;
import com.finance.finance.dto.LoginRequest;
import com.finance.finance.dto.LoginResponse;
import com.finance.finance.dto.RegisterRequest;
import com.finance.finance.dto.RegisterResponse;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtConfig jwtConfig;

    @PostMapping("/login")
    @Operation(
        summary = "🔐 Fazer login no sistema", 
        description = "Autentica o usuário com username e password, retornando um token JWT válido por 1 hora. " +
                     "O token deve ser usado no header 'Authorization: Bearer {token}' para acessar endpoints protegidos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Login realizado com sucesso - Token JWT retornado"),
            @ApiResponse(responseCode = "401", description = "❌ Credenciais inválidas - Username ou password incorretos"),
            @ApiResponse(responseCode = "400", description = "⚠️ Dados inválidos - Verifique o formato dos campos obrigatórios")
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

    @PostMapping("/register")
    @Operation(
        summary = "📝 Registrar novo usuário", 
        description = "Cria uma nova conta de usuário no sistema. " +
                     "Todos os campos são obrigatórios exceto firstName e lastName. " +
                     "O WhatsApp deve seguir o formato internacional (ex: +5511999999999). " +
                     "Novos usuários recebem automaticamente a role 'USER'."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "✅ Usuário criado com sucesso - Conta ativada e pronta para uso"),
            @ApiResponse(responseCode = "400", description = "⚠️ Dados inválidos ou usuário já existe - Verifique username, email ou formato dos dados"),
            @ApiResponse(responseCode = "500", description = "❌ Erro interno do servidor - Tente novamente mais tarde")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Verificar se o username já existe
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Username já existe");
                error.put("message", "Este username já está sendo usado por outro usuário");
                return ResponseEntity.status(400).body(error);
            }

            // Verificar se o email já existe
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email já existe");
                error.put("message", "Este email já está sendo usado por outro usuário");
                return ResponseEntity.status(400).body(error);
            }

            // Criar novo usuário
            User newUser = new User();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            newUser.setWhatsapp(registerRequest.getWhatsapp());
            newUser.setFirstName(registerRequest.getFirstName());
            newUser.setLastName(registerRequest.getLastName());
            newUser.setEnabled(true);
            newUser.setRoles("USER"); // Usuários novos começam como USER

            // Salvar no banco de dados
            User savedUser = userRepository.save(newUser);

            // Criar resposta
            RegisterResponse registerResponse = new RegisterResponse(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getWhatsapp(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    savedUser.getRoles(),
                    savedUser.getEnabled(),
                    savedUser.getCreatedAt()
            );

            return ResponseEntity.status(201).body(registerResponse);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno do servidor");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/logout")
    @Operation(
        summary = "🚪 Fazer logout", 
        description = "Invalida a sessão atual do usuário e limpa o contexto de segurança. " +
                     "Após o logout, o token JWT ainda será válido até expirar, mas a sessão é encerrada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Logout realizado com sucesso - Sessão encerrada")
    })
    public ResponseEntity<Map<String, String>> logout() {
        SecurityContextHolder.clearContext();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout realizado com sucesso");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(
        summary = "👤 Informações do usuário atual", 
        description = "Retorna as informações completas do usuário autenticado, incluindo dados pessoais, " +
                     "roles e status da conta. Requer token JWT válido no header Authorization."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Informações obtidas com sucesso - Dados do usuário retornados"),
            @ApiResponse(responseCode = "401", description = "❌ Não autorizado - Token JWT inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "⚠️ Usuário não encontrado - Conta pode ter sido removida")
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
