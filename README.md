# agenda-clientes-api

API para gerenciamento de clientes e agendamentos. Java 17 + Spring Boot, PostgreSQL, autenticação via JWT.

## Stack

- Java 17 + Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security)
- PostgreSQL + [Flyway](https://flywaydb.org/) (migrations em `src/main/resources/db/migration`)
- JWT ([jjwt](https://github.com/jwtk/jjwt)) para autenticação stateless
- [springdoc-openapi](https://springdoc.org/) — Swagger UI gerado a partir das anotações dos controllers
- Maven (via wrapper `mvnw`/`mvnw.cmd`)

## Como rodar

O Postgres do projeto (`agenda-clientes-db`) normalmente já está no ar. Verifique com `docker ps --filter name=agenda-clientes-db`; só suba com `docker compose up -d` se o container não aparecer na lista.

```
./mvnw spring-boot:run    # sobe a API em http://localhost:8080; o Flyway aplica as migrations no start
```

No Windows sem shell POSIX, use `mvnw.cmd` no lugar de `./mvnw`.

Se a porta 5432 já estiver em uso, suba o banco em outra porta e aponte a aplicação para ela:

```
AGENDA_DB_PORT=5433 docker compose up -d
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/agenda_clientes ./mvnw spring-boot:run
```

Outros comandos úteis:

```
./mvnw test              # roda a suíte de testes
./mvnw clean verify       # build completo com testes
```

## Documentação interativa (Swagger)

Com a aplicação no ar, a documentação completa dos endpoints (schemas, exemplos, respostas de erro) fica disponível em:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

A doc é pública em desenvolvimento (liberada na `SecurityConfig`). Em produção, pode ser desativada via `SPRINGDOC_API_DOCS_ENABLED=false` e `SPRINGDOC_SWAGGER_UI_ENABLED=false`.

Para chamar os endpoints protegidos pelo Swagger UI: faça login em `POST /api/auth/login`, copie o `token` da resposta e cole no botão **Authorize**.

## Autenticação

Todos os endpoints exigem um JWT no header `Authorization: Bearer <token>`, exceto os de `/api/auth`. O token é obtido em `/api/auth/login` ou `/api/auth/registrar` e expira em `app.jwt.expiration-minutes` (padrão 1440 min / 24h).

Requisição sem token, com token expirado ou inválido recebe **403** (comportamento do Spring Security, não é 401).

## Endpoints

### `/api/auth` — públicos

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/auth/registrar` | Cria uma conta e já devolve o token |
| POST | `/api/auth/login` | Autentica e devolve o token |
| POST | `/api/auth/esqueci-senha` | Gera um token de redefinição (sempre responde 200, mesmo se o email não existir) |
| POST | `/api/auth/redefinir-senha` | Redefine a senha usando o token de redefinição |

### `/api/clientes` — autenticado

Cadastro dos clientes do usuário autenticado (cada usuário só vê os próprios clientes).

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/clientes` | Lista os clientes |
| GET | `/api/clientes/{id}` | Busca um cliente pelo id |
| POST | `/api/clientes` | Cadastra um cliente |
| PUT | `/api/clientes/{id}` | Atualiza um cliente |
| DELETE | `/api/clientes/{id}` | Remove um cliente |

### `/api/agendamentos` — autenticado

Agenda de compromissos com os clientes do usuário autenticado.

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/agendamentos` | Lista os agendamentos; aceita `?data=aaaa-MM-dd` para filtrar um dia |
| GET | `/api/agendamentos/{id}` | Busca um agendamento pelo id |
| POST | `/api/agendamentos` | Cria um agendamento para um cliente |
| PUT | `/api/agendamentos/{id}` | Reagenda (data/hora, duração, observações) |
| PATCH | `/api/agendamentos/{id}/cancelar` | Muda o status para `CANCELADO` |
| PATCH | `/api/agendamentos/{id}/concluir` | Muda o status para `CONCLUIDO` |
| DELETE | `/api/agendamentos/{id}` | Remove um agendamento |

## Formato de erro

Toda resposta de erro segue o formato `ApiError` (`timestamp`, `status`, `message`, `fieldErrors`). `fieldErrors` só é preenchido em erros de validação (400); nos demais fica vazio. O exemplo completo do schema está no Swagger (`ApiError`, em qualquer resposta de erro documentada).

Códigos usados: `400` dados inválidos, `401` credenciais/token de redefinição inválidos, `403` token ausente/expirado/inválido, `404` recurso não encontrado, `409` conflito (ex.: email já cadastrado).

## Configuração (`application.properties`)

| Propriedade | Padrão | Descrição |
|---|---|---|
| `server.port` | `8080` | Porta HTTP |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/agenda_clientes` | Conexão com o Postgres |
| `app.cors.allowed-origins` | `http://localhost:3000` | Origens liberadas no CORS (o front-end `agenda-clientes-web`) |
| `app.jwt.secret` | valor de dev, **trocar em produção** | Segredo de assinatura do JWT (mín. 32 bytes); sobrescrever via `APP_JWT_SECRET` |
| `app.jwt.expiration-minutes` | `1440` | Validade do token, em minutos |

Todas as propriedades podem ser sobrescritas por variável de ambiente (ex.: `SPRING_DATASOURCE_URL`, `APP_JWT_SECRET`, `APP_CORS_ALLOWED_ORIGINS`).

## Estrutura

Organização por feature, com camadas `application` (regra de negócio) / `domain` (entidades) / `infrastructure` (persistência e web):

```
src/main/java/com/agendaclientes/api/
  agendamento/
    application/       # AgendamentoService
    domain/             # Agendamento, AgendamentoStatus
    infrastructure/
      persistence/      # repository JPA
      web/               # controller, DTOs de request/response
  cliente/               # mesma organização
  usuario/                # cadastro, autenticação (AuthService, AuthController)
  shared/
    exception/           # exceções de domínio
    openapi/              # OpenApiConfig (título, versão, esquema de segurança bearer)
    security/              # SecurityConfig, JwtAuthFilter
    web/                    # GlobalExceptionHandler, ApiError
src/main/resources/
  application.properties
  db/migration/           # migrations Flyway (V<versão>__descricao.sql)
```

## Integração com o front-end

O front-end (`agenda-clientes-web`) consome esta API em `VITE_API_URL` (padrão `http://localhost:8080/api`). Ver detalhes de integração no README daquele repositório.
