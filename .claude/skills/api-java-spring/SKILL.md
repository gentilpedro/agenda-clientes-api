---
name: api-java-spring
description: >
  Skill especializada no desenvolvimento, manutenção, correção e revisão
  de APIs Java com Spring/Spring Boot. Use sempre que for implementar
  endpoint, controller, service, repository, DTO, validação, regra de
  negócio, segurança (Spring Security/JWT), persistência (JPA/Hibernate,
  Flyway), tratamento de erros, testes, documentação OpenAPI ou integração
  externa neste projeto — mesmo sem o usuário dizer "Spring" ou "Java",
  bastando algo como "adiciona um endpoint de X", "corrige esse bug na
  API" ou "cria uma migration pra Y". Consulte também antes de revisar
  código, analisar performance/segurança, ou decidir arquitetura/pacotes
  dentro de uma API Java já existente. Respeite a versão de Java, Spring
  e demais dependências já presentes no projeto (verificar pom.xml ou
  build.gradle) e não faça upgrades de versão sem solicitação explícita.
---

# API Java Spring

## Objetivo

Esta skill define padrões para desenvolvimento e manutenção de APIs
utilizando Java e Spring/Spring Boot.

A skill deve ser utilizada para:

- criação de endpoints;
- manutenção de APIs existentes;
- correção de bugs;
- implementação de regras de negócio;
- integração com bancos;
- integração com APIs externas;
- autenticação e autorização;
- validação;
- tratamento de erros;
- testes;
- documentação;
- revisão de código;
- análise de performance;
- análise de segurança.

---

# 1. REGRAS FUNDAMENTAIS

Antes de modificar o projeto:

1. Identificar a versão do Java.
2. Identificar a versão do Spring Boot.
3. Identificar o sistema de build.
4. Identificar a arquitetura existente.
5. Identificar o padrão de organização existente.
6. Ler as configurações relevantes.
7. Entender como os testes estão estruturados.

Não assumir:

- versão do Java;
- versão do Spring Boot;
- banco;
- ORM;
- arquitetura;
- ferramenta de build.

---

# 2. IDENTIFICAÇÃO DA VERSÃO

Verificar primeiro:

Maven:

`pom.xml`

Gradle:

`build.gradle`
`build.gradle.kts`

Verificar:

- Java version;
- Spring Boot version;
- Spring Framework version;
- dependências;
- plugins;
- profiles;
- plugins de testes;
- plugins de qualidade.

Nunca atualizar automaticamente:

- Java;
- Spring Boot;
- Spring Framework;
- Hibernate;
- dependências principais.

Atualizações de versão exigem solicitação explícita.

Antes de usar qualquer dependência (Spring Data JPA, Flyway, Bean
Validation, Spring MVC, Spring Security, JWT, etc.), confirmar no
`pom.xml`/`build.gradle` que ela já está presente. Não adicionar uma
dependência nova sem necessidade comprovada pela tarefa e sem avisar
o usuário sobre o que está sendo incluído.

---

# 3. BUILD

Identificar se o projeto utiliza Maven ou Gradle.

## Maven

Respeitar o `pom.xml`. Usar sempre o wrapper (`mvnw`/`mvnw.cmd`), nunca
um `mvn` global, para garantir a versão fixada no projeto.

Comandos comuns:

```bash
./mvnw clean verify
./mvnw test
./mvnw spring-boot:run
./mvnw clean package
```

No Windows, usar `mvnw.cmd` no lugar de `./mvnw` quando não houver um
shell POSIX disponível.

## Gradle

Respeitar o `build.gradle`/`build.gradle.kts`. Usar sempre o wrapper
(`gradlew`/`gradlew.bat`).

Comandos comuns:

```bash
./gradlew build
./gradlew test
./gradlew bootRun
```

## Regras gerais de build

- Rodar o build/testes antes de considerar uma alteração concluída.
- Não editar `pom.xml`/`build.gradle` além do estritamente necessário
  para a tarefa (ex.: não reformatar, não reordenar dependências sem
  motivo).
- Se o build falhar por causa alheia à alteração feita (ex.: ambiente,
  banco indisponível), investigar a causa raiz antes de assumir que é
  um problema no código.

---

# 4. ARQUITETURA E ORGANIZAÇÃO

Antes de criar qualquer classe nova, identificar o padrão de pacotes
já usado no projeto (por exemplo `controller`/`service`/`repository`/
`domain`/`dto`, ou uma organização por feature/módulo) e segui-lo.

Camadas típicas de uma API Spring, quando presentes no projeto:

- **Controller**: recebe a requisição HTTP, valida entrada (via
  `@Valid`) e delega para a camada de serviço. Não deve conter regra
  de negócio.
- **Service**: contém a regra de negócio. Não deve depender de
  detalhes de HTTP (`HttpServletRequest`, `ResponseEntity`, etc.).
- **Repository**: acesso a dados, tipicamente via Spring Data JPA
  (`interface XRepository extends JpaRepository<...>`).
- **DTO**: objetos de entrada/saída da API, desacoplados das entidades
  JPA.
- **Domain/Entity**: entidades JPA, mapeadas com `@Entity`.

Não introduzir uma camada, padrão (ex.: CQRS, Hexagonal, Ports &
Adapters) ou biblioteca de mapeamento (MapStruct, ModelMapper) que o
projeto ainda não usa, a menos que solicitado. Se o projeto for
pequeno e não seguir uma arquitetura em camadas explícita, não impor
uma — seguir o que já existe.

---

# 5. DTOs E VALIDAÇÃO

- Endpoints que recebem corpo de requisição devem receber um DTO
  dedicado, não a entidade JPA diretamente (evita expor campos internos
  e vazar `@Entity` para a camada HTTP).
- Validar entrada com Bean Validation (`jakarta.validation` —
  `@NotNull`, `@NotBlank`, `@Size`, `@Email`, etc.) nos campos do DTO,
  e `@Valid`/`@Validated` no parâmetro do controller.
- Regras de validação que dependem de estado (ex.: unicidade,
  consistência entre campos, verificação em banco) pertencem à camada
  de serviço, não ao DTO.
- Não duplicar validação já garantida pelo banco (ex.: constraint
  `UNIQUE`) sem necessidade — mas tratar a violação de forma amigável
  (ver seção 6).

---

# 6. TRATAMENTO DE ERROS

- Centralizar tratamento de exceções com `@RestControllerAdvice` /
  `@ExceptionHandler`, se o projeto já tiver esse padrão — caso
  contrário, verificar como erros já são tratados antes de introduzir
  um mecanismo novo.
- Retornar códigos HTTP coerentes com o erro (400 para validação,
  401/403 para autenticação/autorização, 404 para recurso inexistente,
  409 para conflito, 500 apenas para erro inesperado).
- Não expor detalhes internos (stack trace, mensagem de exceção crua
  do banco) diretamente na resposta ao cliente. Logar o detalhe
  internamente e devolver uma mensagem de erro estruturada e segura.
- Usar exceções específicas de domínio (ex.: `ResourceNotFoundException`,
  `BusinessRuleException`) em vez de `RuntimeException` genérica,
  quando esse padrão já existir no projeto.

---

# 7. SEGURANÇA (SPRING SECURITY / JWT)

- Identificar a `SecurityFilterChain` (ou configuração equivalente)
  já existente antes de alterar regras de autorização — não
  reescrever a configuração de segurança do zero.
- Toda alteração em autenticação, autorização, geração/validação de
  token ou liberação de endpoint público é sensível: revisar com
  atenção redobrada e explicar ao usuário o que está sendo liberado
  ou restringido.
- JWT: respeitar a biblioteca já usada no projeto (ex.: `jjwt`). Nunca
  logar o token completo, o segredo de assinatura, nem hardcodear
  segredos no código — eles devem vir de configuração/variável de
  ambiente.
- Senhas de usuário devem sempre ser armazenadas com hash (ex.:
  `BCryptPasswordEncoder`), nunca em texto puro.
- Não desabilitar CSRF, CORS ou validação de token como atalho para
  "fazer funcionar" sem entender e comunicar a implicação de segurança.
- Ao criar endpoint novo, definir explicitamente se ele é público ou
  autenticado, e com qual(is) papel(is)/escopo(s) — não deixar a
  regra de acesso implícita.

---

# 8. PERSISTÊNCIA (JPA/HIBERNATE E FLYWAY)

- Alterações de schema (nova tabela, nova coluna, novo índice,
  constraint) devem ser feitas via migration (ex.: Flyway, em
  `src/main/resources/db/migration`, seguindo a convenção de nome já
  usada, tipicamente `V<versão>__descricao.sql`), nunca via
  `ddl-auto: update`/`create` em produção.
- Migrations já aplicadas não devem ser editadas — criar uma nova
  migration para corrigir ou evoluir o schema.
- Mapear entidades JPA com atenção a:
  - tipo de relacionamento (`@OneToMany`, `@ManyToOne`, etc.) e seu
    impacto em performance (evitar `EAGER` sem necessidade, cuidado
    com N+1 — usar `@EntityGraph`, `JOIN FETCH` ou projeções quando
    apropriado);
  - cascade e orphanRemoval, só quando o domínio realmente exigir;
  - índices e constraints refletidos também na migration.
- Queries: preferir Spring Data JPA (métodos derivados, `@Query`) ao
  já padrão do projeto. Evitar concatenar SQL dinamicamente (risco de
  SQL injection) — usar parâmetros nomeados/posicionais sempre.
- Não trocar o banco, o ORM ou a estratégia de migration sem pedido
  explícito.

---

# 9. TESTES

- Identificar como os testes já estão organizados (unitários vs. de
  integração, uso de `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`,
  Testcontainers, banco em memória, etc.) e seguir o mesmo padrão.
- Toda regra de negócio nova ou corrigida deve vir acompanhada de
  teste que comprove o comportamento (incluindo o caso do bug, quando
  for uma correção).
- Testes de controller devem cobrir o contrato HTTP (status code,
  corpo da resposta, validação de entrada), não reimplementar a
  lógica de serviço.
- Testes de repositório/persistência devem usar um banco real ou
  equivalente (ex.: Testcontainers com PostgreSQL), não H2, quando o
  projeto já depender de recursos específicos do PostgreSQL (JSONB,
  funções nativas, etc.) — mas só migrar a estratégia de teste
  existente se isso for pedido.
- Rodar a suíte de testes (`./mvnw test` ou `./gradlew test`) antes de
  considerar a tarefa concluída.

---

# 10. DOCUMENTAÇÃO (OPENAPI)

- Se o projeto já expõe OpenAPI/Swagger (ex.: springdoc-openapi),
  manter os endpoints novos documentados no mesmo padrão (anotações
  `@Operation`, `@ApiResponse`, ou geração automática a partir dos
  DTOs — conforme já usado).
- Não adicionar a dependência de OpenAPI a um projeto que não a tem
  sem solicitação explícita.
- DTOs devem ter nomes e campos autoexplicativos; usar `@Schema`/
  javadoc apenas quando o significado de um campo não for óbvio pelo
  nome e tipo.

---

# 11. REVISÃO DE CÓDIGO, PERFORMANCE E SEGURANÇA

Ao revisar ou finalizar uma alteração, verificar:

**Correção**
- A regra de negócio implementada bate com o que foi pedido?
- Casos de borda (entrada nula, lista vazia, recurso inexistente)
  estão tratados?

**Performance**
- Existe N+1 introduzido em algum relacionamento JPA?
- Alguma query roda dentro de um loop que poderia ser uma única
  consulta/batch?
- Endpoints que retornam listas grandes têm paginação
  (`Pageable`/`Page`), quando o projeto já usa esse padrão?

**Segurança**
- Entrada do usuário é validada antes de uso?
- Algum dado sensível (senha, token, segredo) está sendo logado ou
  retornado na resposta?
- Autorização do endpoint está correta e explícita?
- Alguma query é montada por concatenação de string com entrada do
  usuário (risco de SQL injection)?

**Consistência com o projeto**
- O código novo segue o mesmo estilo, pacotes e convenções de nomes
  já usados no projeto, em vez de introduzir um padrão paralelo?
- Nenhuma versão de Java, Spring ou dependência foi alterada sem
  pedido explícito?
