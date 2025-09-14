# Credenciais de Usuário - Sistema Finance

## Usuário Principal

**Username:** `kaiqueyamamoto`  
**Password:** `B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8=`  
**Email:** `kaiqueyamamoto@example.com`  
**Roles:** `USER,ADMIN`

## Como Usar

### 1. Acesso ao Swagger
- URL: http://localhost:8080/swagger-ui/index.html
- Não requer autenticação para visualizar

### 2. Teste de Endpoints
1. Na interface Swagger, clique em "Authorize"
2. Insira o token JWT (obtido através de login)
3. Teste os endpoints protegidos

### 3. Login via API
```bash
POST /auth/login
{
  "username": "kaiqueyamamoto",
  "password": "B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8="
}
```

## Configurações de Segurança

- **JWT Secret:** FinanceAppSecretKey2024!@#$%^&*()
- **Session Timeout:** 3600 segundos (1 hora)
- **Environment:** Development

## Notas Importantes

⚠️ **ATENÇÃO:** Esta senha é para desenvolvimento. Em produção, use senhas mais complexas e armazene-as de forma segura.

🔒 **Segurança:** Esta documentação contém informações sensíveis. Mantenha-a em local seguro.

## Próximos Passos

1. Implementar endpoint de login
2. Configurar geração de tokens JWT
3. Testar autenticação no Swagger
4. Configurar variáveis de ambiente para produção
