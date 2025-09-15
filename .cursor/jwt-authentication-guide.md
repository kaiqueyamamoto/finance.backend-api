# Guia de Autenticação JWT - Finance API

## 🔐 Visão Geral

A API Finance utiliza autenticação baseada em JWT (JSON Web Token) para segurança. Este guia explica como usar o sistema de autenticação implementado.

## 🚀 Como Funciona

### 1. Fluxo de Autenticação

```mermaid
sequenceDiagram
    participant Client as Cliente
    participant API as Finance API
    participant Auth as AuthController
    participant JWT as JwtConfig
    
    Client->>API: POST /auth/login (username, password)
    API->>Auth: Validar credenciais
    Auth->>JWT: Gerar token JWT
    JWT-->>Auth: Token JWT
    Auth-->>Client: LoginResponse (token + dados)
    Client->>API: Request com Authorization: Bearer {token}
    API->>JWT: Validar token
    JWT-->>API: Token válido
    API-->>Client: Resposta da API
```

### 2. Componentes da Autenticação

#### JwtConfig
- Gera e valida tokens JWT
- Configura expiração (1 hora por padrão)
- Usa chave secreta para assinatura

#### JwtAuthenticationFilter
- Intercepta todas as requisições
- Extrai token do header Authorization
- Valida token e configura contexto de segurança

#### AuthController
- Endpoints de login/logout
- Validação de credenciais
- Retorno de informações do usuário

## 📋 Endpoints de Autenticação

### POST /auth/login
**Descrição:** Autentica o usuário e retorna token JWT

**Request:**
```json
{
  "username": "kaiqueyamamoto",
  "password": "B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8="
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJrYWlxdWV5YW1hbW90byIsInJvbGVzIjoiVVNFUixBRE1JTiIsImlhdCI6MTcwNTMyNDAwMCwiZXhwIjoxNzA1MzI3NjAwfQ...",
  "type": "Bearer",
  "username": "kaiqueyamamoto",
  "email": "kaiqueyamamoto@example.com",
  "roles": "USER,ADMIN",
  "expiresAt": "2024-01-15T15:30:00"
}
```

**Response (401):**
```json
{
  "error": "Credenciais inválidas",
  "message": "Username ou password incorretos"
}
```

### POST /auth/logout
**Descrição:** Invalida a sessão do usuário

**Request:** Nenhum corpo necessário

**Response (200):**
```json
{
  "message": "Logout realizado com sucesso"
}
```

### GET /auth/me
**Descrição:** Retorna informações do usuário autenticado

**Headers:** `Authorization: Bearer {token}`

**Response (200):**
```json
{
  "id": 1,
  "username": "kaiqueyamamoto",
  "email": "kaiqueyamamoto@example.com",
  "roles": "USER,ADMIN",
  "enabled": true,
  "createdAt": "2024-01-15T10:00:00"
}
```

### GET /auth/validate
**Descrição:** Valida se o token JWT é válido

**Headers:** `Authorization: Bearer {token}`

**Response (200):**
```json
{
  "valid": true,
  "username": "kaiqueyamamoto",
  "authorities": ["USER", "ADMIN"]
}
```

## 🔧 Configuração

### application.properties
```properties
# JWT Configuration
jwt.secret=FinanceAppSecretKey2024!@#$%^&*()
jwt.expiration=3600000  # 1 hora em milissegundos
```

### SecurityConfig
- Configuração stateless (sem sessão)
- Filtro JWT adicionado antes da autenticação
- Endpoints públicos configurados

## 🛠️ Como Usar no Swagger

### 1. Fazer Login
1. Acesse http://localhost:8080/swagger-ui/index.html
2. Encontre o endpoint `POST /auth/login`
3. Clique em "Try it out"
4. Insira as credenciais:
   ```json
   {
     "username": "kaiqueyamamoto",
     "password": "B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8="
   }
   ```
5. Execute e copie o token da resposta

### 2. Autorizar no Swagger
1. Clique no botão "Authorize" (🔒)
2. Insira o token no formato: `Bearer {seu-token}`
3. Clique em "Authorize"
4. Agora você pode testar endpoints protegidos

### 3. Testar Endpoints Protegidos
- Todos os endpoints com tag "Dashboard", "Categories", "Cash Flow" requerem autenticação
- O header `Authorization: Bearer {token}` será adicionado automaticamente

## 🔒 Segurança

### Token JWT
- **Algoritmo:** HS512
- **Expiração:** 1 hora (configurável)
- **Claims:** username, roles, iat, exp

### Headers de Segurança
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Validação
- Token é validado a cada requisição
- Verificação de expiração automática
- Extração de roles para autorização

## 📱 Exemplos de Uso

### cURL
```bash
# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "kaiqueyamamoto",
    "password": "B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8="
  }'

# Usar token
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer {seu-token}"
```

### JavaScript (Fetch)
```javascript
// Login
const loginResponse = await fetch('http://localhost:8080/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    username: 'kaiqueyamamoto',
    password: 'B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8='
  })
});

const loginData = await loginResponse.json();
const token = loginData.token;

// Usar token
const userResponse = await fetch('http://localhost:8080/auth/me', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

## ⚠️ Troubleshooting

### Token Expirado
**Erro:** `Token expirado`
**Solução:** Faça login novamente para obter novo token

### Token Inválido
**Erro:** `Token inválido`
**Solução:** Verifique se o token está no formato correto: `Bearer {token}`

### Não Autorizado
**Erro:** `401 Unauthorized`
**Solução:** Verifique se o token está sendo enviado corretamente no header

### Credenciais Inválidas
**Erro:** `Credenciais inválidas`
**Solução:** Verifique username e password

## 🔄 Próximos Passos

1. **Refresh Token:** Implementar renovação automática de tokens
2. **Blacklist:** Implementar lista negra para tokens invalidados
3. **Rate Limiting:** Adicionar limite de tentativas de login
4. **Audit Log:** Log de tentativas de autenticação
5. **Multi-factor Auth:** Implementar 2FA

## 📚 Recursos Adicionais

- [JWT.io](https://jwt.io/) - Debugger de tokens JWT
- [Spring Security JWT](https://spring.io/guides/topicals/spring-security-architecture/) - Documentação oficial
- [JJWT Library](https://github.com/jwtk/jjwt) - Biblioteca Java JWT
