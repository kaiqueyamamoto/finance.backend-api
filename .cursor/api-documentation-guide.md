# Guia de Documentação da API Finance

## 🔐 Credenciais de Acesso

**Usuário:** `kaiqueyamamoto`  
**Senha:** `B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8=`

> ⚠️ **Importante:** Esta senha é para desenvolvimento. Em produção, use senhas mais seguras.

## Como Usar a Documentação Swagger

### 1. Iniciando a Aplicação

```bash
# Navegar para o diretório do projeto
cd /mnt/d/Developer/zapdev/finance/finance.backend-api

# Compilar e executar a aplicação
mvn spring-boot:run
```

### 2. Acessando a Interface Swagger

Após iniciar a aplicação, acesse:
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

### 3. Navegando pela Documentação

#### Estrutura da Interface
- **Tags:** Organizam os endpoints por funcionalidade
  - Test: Endpoints para testes
  - Dashboard: Visualização de dados financeiros
  - Categories: Gerenciamento de categorias
  - Cash Flow: Gerenciamento de fluxo de caixa

#### Testando Endpoints
1. **Expandir um endpoint:** Clique na seta ao lado do método HTTP
2. **Visualizar parâmetros:** Veja os parâmetros necessários
3. **Testar endpoint:** Clique em "Try it out"
4. **Inserir dados:** Preencha os campos necessários
5. **Executar:** Clique em "Execute"

### 4. Autenticação

#### Credenciais de Usuário
**Username:** `kaiqueyamamoto`  
**Password:** `B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8=`

#### Configuração Bearer Token
1. Na interface Swagger, clique no botão "Authorize"
2. Insira o token JWT no formato: `Bearer {seu-token}`
3. Clique em "Authorize"
4. Agora você pode testar endpoints protegidos

#### Login via API
```bash
POST /auth/login
{
  "username": "kaiqueyamamoto",
  "password": "B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8="
}
```

#### Exemplo de Token
```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 5. Endpoints Principais

#### Test (Desenvolvimento)
- `POST /auth/test/create-user` - Criar usuário de teste
- `POST /auth/test/cashflow` - Criar fluxo de caixa de teste
- `GET /auth/test/dashboard` - Dashboard de teste

#### Dashboard
- `GET /auth/dashboard/overview` - Visão geral financeira
- `GET /auth/dashboard/monthly-summary` - Resumo anual
- `GET /auth/dashboard/quick-stats` - Estatísticas rápidas

#### Categories
- `GET /auth/categories` - Listar categorias
- `POST /auth/categories` - Criar categoria
- `PUT /auth/categories/{id}` - Atualizar categoria

#### Cash Flow
- `GET /auth/cashflow` - Listar transações
- `POST /auth/cashflow` - Criar transação
- `GET /auth/cashflow/summary` - Resumo financeiro

### 6. Códigos de Resposta HTTP

#### Sucesso
- `200 OK` - Requisição bem-sucedida
- `201 Created` - Recurso criado com sucesso
- `204 No Content` - Recurso deletado com sucesso

#### Erro do Cliente
- `400 Bad Request` - Dados inválidos
- `401 Unauthorized` - Não autorizado
- `404 Not Found` - Recurso não encontrado

#### Erro do Servidor
- `500 Internal Server Error` - Erro interno

### 7. Exemplos de Uso

#### Criar uma Categoria
```json
POST /auth/categories
{
  "name": "Alimentação",
  "description": "Gastos com comida e bebida",
  "type": "EXPENSE"
}
```

#### Criar um Fluxo de Caixa
```json
POST /auth/cashflow
{
  "amount": 1500.00,
  "description": "Salário",
  "type": "INCOME",
  "categoryId": 1,
  "date": "2024-01-15"
}
```

#### Obter Resumo Financeiro
```
GET /auth/cashflow/summary?startDate=2024-01-01&endDate=2024-01-31
```

### 8. Dicas de Uso

#### Parâmetros de Data
- Formato: `YYYY-MM-DD`
- Exemplo: `2024-01-15`

#### Paginação
- Use `page` e `size` para controlar paginação
- Exemplo: `?page=0&size=20`

#### Busca
- Use `search` para buscar por termo
- Exemplo: `?term=alimentação`

### 9. Troubleshooting

#### Problema: 401 Unauthorized
**Solução:** Configure o token Bearer na interface Swagger

#### Problema: 400 Bad Request
**Solução:** Verifique os dados enviados no corpo da requisição

#### Problema: 404 Not Found
**Solução:** Verifique se o ID do recurso existe

#### Problema: Interface não carrega
**Solução:** Verifique se a aplicação está rodando na porta 8080

### 10. Recursos Adicionais

#### Exportar Documentação
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **YAML:** http://localhost:8080/v3/api-docs.yaml

#### Integração com Postman
1. Importe o OpenAPI JSON no Postman
2. Configure a autenticação Bearer Token
3. Teste os endpoints diretamente no Postman

#### Integração com Frontend
- Use o OpenAPI JSON para gerar clientes SDK
- Ferramentas recomendadas: OpenAPI Generator, Swagger Codegen

### 11. Manutenção da Documentação

#### Atualizando Anotações
- Modifique as anotações nos controllers
- A documentação será atualizada automaticamente

#### Adicionando Novos Endpoints
1. Crie o endpoint no controller
2. Adicione as anotações `@Operation`, `@ApiResponses`
3. A documentação será incluída automaticamente

#### Melhorando a Documentação
- Adicione exemplos nos DTOs
- Inclua mais detalhes nas descrições
- Documente códigos de erro específicos
