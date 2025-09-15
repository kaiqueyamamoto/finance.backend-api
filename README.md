# 💰 Finance API - Sistema de Gestão Financeira Pessoal

## 📋 Product Requirements Document (PRD)

### 🎯 Visão Geral do Produto

A **Finance API** é uma aplicação backend desenvolvida em Spring Boot para gestão financeira pessoal, oferecendo funcionalidades completas de controle de receitas, despesas, categorização de transações e dashboards analíticos.

---

## 🚀 Funcionalidades Principais

### 1. **Sistema de Autenticação e Autorização**
- **JWT Token-based Authentication** com expiração de 1 hora
- **Registro de usuários** com validação de dados
- **Login/Logout** seguro
- **Validação de tokens** em tempo real
- **Controle de acesso** baseado em roles (USER, ADMIN)

### 2. **Gestão de Categorias**
- **Criação de categorias** personalizadas (Receitas/Despesas)
- **Categorias padrão** pré-configuradas
- **Busca e filtros** por tipo de categoria
- **Estatísticas** de uso das categorias
- **CRUD completo** (Create, Read, Update, Delete)

### 3. **Controle de Fluxo de Caixa**
- **Registro de transações** (Receitas e Despesas)
- **Categorização automática** das transações
- **Transações recorrentes** (Diária, Semanal, Mensal, Anual)
- **Filtros avançados** por período, tipo e categoria
- **Busca textual** nas descrições
- **Resumos financeiros** por período

### 4. **Dashboard e Analytics**
- **Visão geral financeira** com métricas principais
- **Resumos mensais** e anuais
- **Estatísticas rápidas** de receitas vs despesas
- **Métricas de performance** com Prometheus
- **Gráficos e relatórios** (via API)

### 5. **Sistema de Monitoramento**
- **Health checks** para verificação de saúde da API
- **Métricas Prometheus** para observabilidade
- **Logs estruturados** para debugging
- **Contadores de uso** para cada funcionalidade

---

## 🏗️ Arquitetura Técnica

### **Stack Tecnológico**
- **Framework:** Spring Boot 4.0.0-SNAPSHOT
- **Java:** Version 24
- **Database:** PostgreSQL 16
- **ORM:** JPA/Hibernate
- **Security:** Spring Security + JWT
- **Documentation:** Swagger/OpenAPI 3
- **Monitoring:** Micrometer + Prometheus
- **Build:** Maven

### **Estrutura do Projeto**
```
src/main/java/com/finance/finance/
├── config/          # Configurações (Security, JWT, Swagger)
├── controller/      # Controllers REST
├── dto/            # Data Transfer Objects
├── entity/         # Entidades JPA
├── repository/     # Repositórios de dados
├── service/        # Lógica de negócio
└── util/          # Utilitários
```

---

## 🔧 Configuração e Instalação

### **Pré-requisitos**
- Java 24+
- Maven 3.6+
- PostgreSQL 16+
- Docker (opcional)

### **Configuração do Banco de Dados**
```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_db
spring.datasource.username=finance_user
spring.datasource.password=finance_password
```

### **Executando a Aplicação**
```bash
# Compilar e executar
mvn spring-boot:run

# Ou com Docker
docker-compose up
```

### **Acessos**
- **API Base:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Health Check:** http://localhost:8080/auth/health
- **Prometheus:** http://localhost:8080/actuator/prometheus

---

## 📚 Documentação da API

### **Autenticação**
Todos os endpoints (exceto login/register) requerem autenticação via JWT:

```bash
# Login
POST /auth/login
{
  "username": "kaiqueyamamoto",
  "password": "B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8="
}

# Usar token nas requisições
Authorization: Bearer {jwt_token}
```

### **Endpoints Principais**

#### **🔐 Autenticação**
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/auth/login` | Fazer login e obter JWT |
| POST | `/auth/register` | Registrar novo usuário |
| POST | `/auth/logout` | Fazer logout |
| GET | `/auth/me` | Informações do usuário atual |
| GET | `/auth/validate` | Validar token JWT |

#### **📊 Dashboard**
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/auth/dashboard/overview` | Visão geral financeira |
| GET | `/auth/dashboard/monthly-summary` | Resumo mensal |
| GET | `/auth/dashboard/quick-stats` | Estatísticas rápidas |

#### **🏷️ Categorias**
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/auth/categories` | Listar todas as categorias |
| POST | `/auth/categories` | Criar nova categoria |
| GET | `/auth/categories/type/{type}` | Filtrar por tipo |
| GET | `/auth/categories/{id}` | Obter categoria por ID |
| PUT | `/auth/categories/{id}` | Atualizar categoria |
| DELETE | `/auth/categories/{id}` | Deletar categoria |
| POST | `/auth/categories/initialize` | Inicializar categorias padrão |
| GET | `/auth/categories/stats` | Estatísticas das categorias |

#### **💰 Fluxo de Caixa**
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/auth/cashflow` | Listar transações (paginado) |
| POST | `/auth/cashflow` | Criar nova transação |
| GET | `/auth/cashflow/{id}` | Obter transação por ID |
| PUT | `/auth/cashflow/{id}` | Atualizar transação |
| DELETE | `/auth/cashflow/{id}` | Deletar transação |
| GET | `/auth/cashflow/summary` | Resumo financeiro |
| GET | `/auth/cashflow/date-range` | Filtrar por período |
| GET | `/auth/cashflow/type/{type}` | Filtrar por tipo |
| GET | `/auth/cashflow/search` | Buscar transações |

#### **🔍 Testes e Monitoramento**
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/auth/health` | Health check da aplicação |
| POST | `/auth/test/create-user` | Criar usuário de teste |
| POST | `/auth/test/cashflow` | Criar dados de teste |
| GET | `/auth/test/dashboard` | Dashboard de teste |

---

## 📊 Modelos de Dados

### **User (Usuário)**
```json
{
  "id": 1,
  "username": "kaiqueyamamoto",
  "email": "kaiqueyamamoto@example.com",
  "roles": "USER,ADMIN",
  "enabled": true,
  "createdAt": "2025-09-14T20:57:31.992821"
}
```

### **Category (Categoria)**
```json
{
  "id": 1,
  "name": "Alimentação",
  "description": "Gastos com comida e bebida",
  "type": "EXPENSE",
  "isActive": true,
  "createdAt": "2025-09-14T20:57:31.992821"
}
```

### **CashFlow (Transação)**
```json
{
  "id": 1,
  "description": "Salário",
  "amount": 5000.00,
  "transactionDate": "2025-09-15",
  "type": "INCOME",
  "category": {
    "id": 1,
    "name": "Salário"
  },
  "isRecurring": false,
  "notes": "Salário mensal",
  "createdAt": "2025-09-14T20:57:31.992821"
}
```

---

## 🔒 Segurança

### **Autenticação JWT**
- **Algoritmo:** HS512
- **Expiração:** 1 hora (3600000ms)
- **Chave secreta:** Configurável via application.properties
- **Header:** `Authorization: Bearer {token}`

### **Autorização**
- **Roles:** USER, ADMIN
- **Proteção:** Todos os endpoints exceto login/register
- **Validação:** Token validado a cada requisição

### **Validação de Dados**
- **Bean Validation** com anotações JSR-303
- **Sanitização** de inputs
- **Validação** de tipos e formatos

---

## 📈 Monitoramento e Observabilidade

### **Métricas Prometheus**
- `finance.dashboard.access` - Acessos ao dashboard
- `finance.category.created` - Categorias criadas
- `finance.category.updated` - Categorias atualizadas
- `finance.category.deleted` - Categorias deletadas
- `finance.health.checks` - Health checks realizados

### **Health Checks**
- **Endpoint:** `/auth/health`
- **Métricas:** Contador de acessos
- **Status:** 200 OK quando saudável

### **Logs**
- **Nível DEBUG** para Spring Security
- **Nível DEBUG** para Hibernate SQL
- **Logs estruturados** para debugging

---

## 🧪 Testes

### **Script de Teste Automatizado**
```bash
# Executar testes de autenticação
./test-auth.sh
```

### **Testes Manuais via Swagger**
1. Acesse http://localhost:8080/swagger-ui/index.html
2. Faça login via `/auth/login`
3. Configure o token no botão "Authorize"
4. Teste os endpoints protegidos

### **Credenciais de Desenvolvimento**
- **Username:** `kaiqueyamamoto`
- **Password:** `B4aHwdyVX5RXal08eHzzGzydfAFHUAMhW7s61bktGU8=`
- **Roles:** `USER,ADMIN`

---

## 🚀 Deploy e Produção

### **Docker Compose**
```yaml
services:
  api:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/finance_db
  db:
    image: postgres:16-alpine
    environment:
      - POSTGRES_DB=finance_db
      - POSTGRES_USER=finance_user
      - POSTGRES_PASSWORD=finance_password
```

### **Variáveis de Ambiente**
- `SPRING_DATASOURCE_URL` - URL do banco de dados
- `SPRING_DATASOURCE_USERNAME` - Usuário do banco
- `SPRING_DATASOURCE_PASSWORD` - Senha do banco
- `JWT_SECRET` - Chave secreta para JWT
- `JWT_EXPIRATION` - Tempo de expiração do token

---

## 📝 Roadmap e Melhorias Futuras

### **Funcionalidades Planejadas**
- [ ] **Relatórios PDF** para exportação
- [ ] **Gráficos interativos** no dashboard
- [ ] **Notificações** para transações recorrentes
- [ ] **Metas financeiras** e acompanhamento
- [ ] **Integração com bancos** via Open Banking
- [ ] **App mobile** nativo
- [ ] **Backup automático** dos dados

### **Melhorias Técnicas**
- [ ] **Cache Redis** para performance
- [ ] **Rate limiting** para proteção
- [ ] **Auditoria completa** de transações
- [ ] **Testes de integração** automatizados
- [ ] **CI/CD pipeline** completo

---

## 🤝 Contribuição

### **Como Contribuir**
1. Fork o repositório
2. Crie uma branch para sua feature
3. Faça commit das mudanças
4. Abra um Pull Request

### **Padrões de Código**
- **Java:** Seguir convenções do Spring Boot
- **Commits:** Usar conventional commits
- **Documentação:** Manter README atualizado
- **Testes:** Escrever testes para novas funcionalidades

---

## 📞 Suporte

### **Documentação Adicional**
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **API Docs:** http://localhost:8080/v3/api-docs
- **Health Check:** http://localhost:8080/auth/health

### **Logs e Debugging**
- **Logs da aplicação:** Console output
- **Métricas:** http://localhost:8080/actuator/prometheus
- **Health:** http://localhost:8080/actuator/health

---

## 📄 Licença

Este projeto está sob licença MIT. Veja o arquivo LICENSE para mais detalhes.

---

**Desenvolvido com ❤️ usando Spring Boot e Java 24**
