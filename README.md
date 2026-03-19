# Backend Java Developer — Finch Soluções

API REST para gerenciamento e sincronização de TV Shows, construída com **Java 25**, **Spring Boot 3.5**, **PostgreSQL 16** e **Docker**.

---

## Tecnologias

| Tecnologia          | Versão    |
|---------------------|-----------|
| Java                | 25        |
| Spring Boot         | 3.5.7     |
| PostgreSQL          | 16        |
| Flyway              | Integrado |
| Spring Security     | JWT/HS256 |
| SpringDoc (Swagger) | 2.8.x     |
| Docker Compose      | —         |
| Testcontainers      | Testes    |

---

## Como executar com Docker (recomendado)

### Pré-requisitos
- Docker e Docker Compose instalados

### 1. Build e start

```bash
docker compose up --build
```

A aplicação sobe na porta **9012** e o banco PostgreSQL na porta **5432**.

### 2. Acessar Swagger UI

```
http://localhost:9012/swagger-ui.html
```

---

## Como executar localmente (sem Docker)

### Pré-requisitos
- Java 25+
- PostgreSQL 16 rodando localmente (banco: `meubanco`, porta `5432`)

```sql
CREATE DATABASE meubanco;
```

### Rodar

```bash
./mvnw spring-boot:run
```

O Flyway criará as tabelas automaticamente (V1, V2, V3).

---

## Credenciais padrão

| Campo   | Valor   |
|---------|---------|
| Usuário | `admin` |
| Senha   | `admin` |
| Role    | `ADMIN` |

---

## Endpoints principais

### Autenticação
| Método | Endpoint          | Auth | Descrição      |
|--------|-------------------|------|----------------|
| POST   | `/api/auth/login` | Não  | Gera token JWT |

### Usuários
| Método | Endpoint          | Role  | Descrição                 |
|--------|-------------------|-------|---------------------------|
| POST   | `/api/users`      | ADMIN | Cria usuário              |
| GET    | `/api/users`      | ADMIN | Lista usuários (paginado) |
| GET    | `/api/users/{id}` | ANY   | Busca usuário por ID      |
| PUT    | `/api/users/{id}` | ADMIN | Atualiza usuário          |
| DELETE | `/api/users/{id}` | ADMIN | Remove usuário            |

### Shows
| Método | Endpoint     | Role  | Descrição                          |
|--------|--------------|-------|------------------------------------|
| POST   | `/api/shows` | ADMIN | Sincroniza show via TVMaze API     |
| GET    | `/api/shows` | ANY   | Lista shows (paginação + filtro)   |

### Episódios
| Método | Endpoint                         | Role | Descrição                     |
|--------|----------------------------------|------|-------------------------------|
| GET    | `/api/episodes/average?showId=`  | ANY  | Média de rating por temporada |

---

## Fluxo de uso rápido

### 1. Login como ADMIN
```bash
curl -X POST http://localhost:9012/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
# → { "token": "eyJ..." }
```

### 2. Criar usuário USER
```bash
curl -X POST http://localhost:9012/api/users \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"senha123","role":"USER","enabled":true}'
```

### 3. Sincronizar um show (ADMIN only)
```bash
curl -X POST http://localhost:9012/api/shows \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Breaking Bad"}'
# Persiste o show + todos os episódios da API TVMaze
```

### 4. Listar shows com filtro
```bash
curl "http://localhost:9012/api/shows?name=break&page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

### 5. Média de rating por temporada
```bash
curl "http://localhost:9012/api/episodes/average?showId=<SHOW_ID>" \
  -H "Authorization: Bearer <TOKEN>"
# → [{ "season": 1, "averageRating": 8.45 }, ...]
```

---

## Regras de negócio

- `POST /api/shows` — apenas `ADMIN`
- Duplicatas evitadas pelo `id_integration` (ID do TVMaze)
- Ratings `null` são ignorados no cálculo da média; se todos forem `null` → retorna `0`
- Sem episódios para o show → `404 Not Found`
- Perfil `USER` não pode sincronizar shows (`403 Forbidden`)

---

## Rodar os testes

```bash
./mvnw test
```

> Testes de integração usam **Testcontainers** — Docker precisa estar em execução.

---

## Estrutura do projeto

```
src/main/java/com/cmanager/app/
├── application/
│   ├── controller/   ShowController, EpisodeController
│   ├── data/         ShowDTO, SeasonAverageDTO, ...
│   ├── domain/       Show, Episode (entidades JPA)
│   ├── repository/   ShowRepository, EpisodeRepository
│   └── service/      ShowService, EpisodeService
├── authentication/
│   ├── controller/   AuthController, UserController
│   ├── domain/       User, Role
│   ├── repository/   UserRepository
│   ├── security/     SecurityConfig, JwtConfig
│   └── service/      UserService, AuthenticationService
├── core/
│   ├── data/         ErrorResponse, PageResultResponse
│   ├── exception/    GlobalExceptionHandler
│   └── utils/        Util (paginação)
└── integration/
    ├── client/       AbstractRequest, RequestService (TVMaze)
    ├── config/       RestTemplateConfiguration
    └── dto/          ShowsRequestDTO, EpisodeRequestDTO, RatingDTO

src/main/resources/db/migration/
├── V1__users.sql    tabela users + admin seed
├── V2__show.sql     tabela show
└── V3__episode.sql  tabela episode
```
