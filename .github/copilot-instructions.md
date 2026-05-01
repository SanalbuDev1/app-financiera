# Instrucciones del Proyecto: Finanzas Personales — Backend Java

## Descripción General
Backend de **finanzas personales** construido con Spring Boot que da soporte a un frontend Angular 21 (standalone, signals, SSR) con arquitectura hexagonal y autenticación JWT.

---

## Stack Tecnológico
- **Java 25**
- **Spring Boot 4.0.5**
- **Spring WebFlux** (reactivo — `Mono`/`Flux`, **NO** Spring MVC)
- **Spring Security** con JWT stateless (sin sesiones)
- **PostgreSQL + R2DBC** (acceso reactivo a datos, **NO** JPA/Hibernate)
- **Lombok** (`@Data`, `@Builder`, `@RequiredArgsConstructor`, etc.)
- **Gradle con Groovy DSL** (`build.gradle`, **NO** Maven)
- **springdoc-openapi** (`springdoc-openapi-starter-webflux-ui:3.0.3`) — Swagger UI en `/swagger-ui.html`
- Paquete raíz: `com.finanzas.personales.finanzas`
- Paquete transacciones: `com.finanzas.personales.finanzas.transacciones`
- `@SpringBootApplication` — escanea automáticamente todos los sub-paquetes

---

## Contrato de API (lo que espera el frontend Angular)

### GET /api/health (público)
**Response 200 OK:**
```json
{ "status": "UP", "version": "1.0.0", "timestamp": "2026-04-26T12:00:00Z" }
```

### POST /api/auth/login
**Request:**
```json
{ "email": "string", "password": "string" }
```
**Response 200 OK:**
```json
{ "id": "string", "email": "string", "name": "string", "role": "ADMIN|USER", "token": "string (JWT)" }
```
**Errores:** `401 Unauthorized` si credenciales inválidas.

### POST /api/auth/register
**Request:**
```json
{ "email": "string", "password": "string", "name": "string" }
```
**Response 201 Created:**
```json
{ "id": "string", "email": "string", "name": "string", "role": "USER", "token": "string (JWT)" }
```
**Errores:** `409 Conflict` si el email ya existe.

### GET /api/transactions?page=0&size=15&from=&to=&type=&category=
**Headers:** `Authorization: Bearer <JWT>`
**Response 200 OK:**
```json
{
  "content": [
    {
      "id": "string",
      "description": "string",
      "amount": 150.50,
      "category": "food",
      "type": "expense",
      "date": "2026-04-21",
      "notes": "string",
      "createdAt": "2026-04-21T10:30:00"
    }
  ],
  "totalElements": 30,
  "totalPages": 2,
  "page": 0,
  "size": 15
}
```

### GET /api/transactions/all
**Headers:** `Authorization: Bearer <JWT>`
**Response 200 OK:** array plano con todas las transacciones del usuario (sin paginación).
```json
[
  {
    "id": "string",
    "description": "string",
    "amount": 150.50,
    "category": "food",
    "type": "expense",
    "date": "2026-04-21",
    "notes": "string",
    "createdAt": "2026-04-21T10:30:00"
  }
]
```

### GET /api/transactions/summary?month=4&year=2026
**Headers:** `Authorization: Bearer <JWT>`
**Response 200 OK:**
```json
{
  "totalBalance": 12500.00,
  "monthlyIncome": 4500.00,
  "monthlyExpenses": 2800.00,
  "monthlySavings": 1700.00,
  "savingsGoal": 3000.00
}
```

### POST /api/transactions
**Headers:** `Authorization: Bearer <JWT>`
**Request:**
```json
{
  "description": "string",
  "amount": 150.50,
  "category": "food",
  "type": "expense",
  "transactionDate": "2026-04-21",
  "notes": "string (optional)"
}
```
**Response 201 Created:** mismo shape que un item de `content` (incluye `createdAt`).

### Nota: `transactionDate` vs `date`
- Al **enviar** (POST request): el campo se llama **`transactionDate`**
- Al **recibir** (todas las responses): el campo llega como **`date`** (el backend usa `@JsonProperty("date")`)

### Valores válidos (definidos en tablas maestras en BD)
- **`type`**: `income`, `expense` (tabla `transaction_types`)
- **`category`**: `food`, `transport`, `entertainment`, `health`, `education`, `shopping`, `bills`, `salary`, `freelance`, `investment`, `savings`, `other` (tabla `categories`)
- La validación la realiza el **FK constraint** en PostgreSQL — no hay enums en Java

### PUT /api/transactions/{id}
**Headers:** `Authorization: Bearer <JWT>`
**Request:** mismo shape que POST:
```json
{
  "description": "string",
  "amount": 150.50,
  "category": "food",
  "type": "expense",
  "transactionDate": "2026-04-21",
  "notes": "string (optional)"
}
```
**Response 200 OK:** mismo shape que un item de `content` (incluye `createdAt`).
**Errores:** `404 Not Found` si no existe o no pertenece al usuario.

### DELETE /api/transactions/{id}
**Headers:** `Authorization: Bearer <JWT>`
**Response:** `204 No Content` si se eliminó, `404 Not Found` si no existe o no pertenece al usuario.

---

## Modelos del Frontend (TypeScript — referencia)
```typescript
interface User { id: string; email: string; name: string; role: UserRole; token: string; }
enum UserRole { ADMIN = 'ADMIN', USER = 'USER' }
```

---

## Usuarios Iniciales (dev/testing)

| Email                    | Password  | Role  |
|--------------------------|-----------|-------|
| admin@financiera.com     | admin123  | ADMIN |
| user@financiera.com      | user123   | USER  |

Contraseñas almacenadas con **BCrypt**.

---

## Arquitectura Hexagonal

El dominio es el núcleo; la infraestructura se conecta a él a través de **ports & adapters**.

```
com.finanzas.personales.finanzas/
├── FinanzasApplication.java     (@SpringBootApplication scanBasePackages="com.finanzas.personales")
│
├── auth/
│   ├── domain/
│   │   ├── model/         User.java, UserRole.java (enum)
│   │   ├── port/          UserRepositoryPort.java
│   │   └── usecase/       LoginUseCase.java, RegisterUseCase.java
│   └── infrastructure/
│       ├── adapter/
│       │   ├── in/        AuthController.java   (/api/auth)
│       │   └── out/       R2dbcUserAdapter.java  (implements UserRepositoryPort)
│       ├── entity/        UserEntity.java
│       └── dto/           LoginRequest, RegisterRequest, UserResponse
│
├── health/
│   └── infrastructure/
│       ├── adapter/in/    HealthController.java  (/api/health)
│       └── dto/           HealthResponse.java
│
├── security/
│   ├── domain/
│   │   └── port/          TokenServicePort.java  (interfaz: generateToken, extractId, extractEmail, extractRole, isTokenValid)
│   └── infrastructure/
│       ├── adapter/in/    JwtAuthFilter.java     (WebFilter: principal=userId, credentials=email)
│       ├── config/        SecurityConfig.java    (SecurityWebFilterChain — WebFlux, PasswordEncoder)
│       └── service/       JwtService.java        (implements TokenServicePort — JJWT)
└── config/
    ├── ApplicationConfig.java  (beans de use cases del dominio)
    ├── CorsConfig.java         (CORS para http://localhost:4200)
    ├── R2dbcConfig.java         (ConnectionFactory + pool + DatabaseClient explícito)
    └── SqlQueryLoader.java      (carga y cachea archivos .sql del classpath)

│
├── transacciones/                       ← sub-paquete de finanzas
│   ├── domain/
│   │   ├── model/
│   │   │   ├── TransactionsDto.java         (entidad de dominio — category y type como String)
│   │   │   └── port/
│   │   │       └── TransactionRepositoryPort.java
│   │   └── usecase/
│   │       ├── CreateTransactionUseCase.java
│   │       ├── UpdateTransactionUseCase.java
│   │       ├── ListTransactionsUseCase.java  (paginacion + filtros)
│   │       ├── DeleteTransactionUseCase.java
│   │       └── GetSummaryUseCase.java        (balance, ingresos, gastos, ahorro)
│   └── infrastructure/
│       ├── adapter/
│       │   ├── in/   TransactionController.java  (/api/transactions)
│       │   └── out/  R2dbcTransactionAdapter.java (implements TransactionRepositoryPort)
│       ├── entity/   TransactionEntity, CategoryEntity, TransactionTypeEntity
│       └── dto/      TransactionRequest, TransactionResponse,
│                     TransactionPageResponse<T>, TransactionSummaryResponse
```

### Reglas de dependencia
- `domain/` no depende de Spring, R2DBC ni ningún framework — solo Java puro
- `domain/usecase/` depende únicamente de `domain/model/` y `domain/port/` (interfaces)
- `infrastructure/` depende de `domain/` e implementa sus ports
- Los controladores (`adapter/in/`) llaman a los use cases del dominio
- Los adaptadores de salida (`adapter/out/`) implementan los ports del dominio

---

## JWT
- Payload debe incluir: `sub` (email), `role`, `name`, `id`
- Expiración: **24 horas**
- Secret y expiración configurables por `application.properties`
- Sin OAuth2 / Azure AD — solo JWT propio
- **JwtAuthFilter** establece `UsernamePasswordAuthenticationToken(userId, email, authorities)` — `@AuthenticationPrincipal` retorna el **userId** (no email)
- `TokenServicePort` (interfaz del dominio) define el contrato: `generateToken(User)`, `extractId(token)`, `extractEmail(token)`, `extractRole(token)`, `isTokenValid(token)`
- `JwtService` implementa `TokenServicePort` usando JJWT — los consumidores dependen del port, no de la implementación

---

## Convenciones de Código
- **Lombok** siempre: `@Data`, `@Builder`, `@RequiredArgsConstructor`, etc.
- Use cases: sufijo `UseCase`, **sin ninguna anotación de Spring** (`@Component`, `@Service`, etc.) — se registran como `@Bean` en `ApplicationConfig`. Usar `@RequiredArgsConstructor` de Lombok para el constructor; no escribir constructores manuales.
- Ports (interfaces del dominio): sufijo `Port` (ej. `UserRepositoryPort`)
- Adaptadores de entrada: sufijo `Controller`, anotados con `@RestController`
- Adaptadores de salida: prefijo con tecnología (ej. `R2dbcUserAdapter`), implementan un port
- DTOs: sufijo `Request`/`Response`, solo en la capa `infrastructure/dto/`
- Modelos de dominio: en `domain/model/`, sin anotaciones de frameworks
- Entidades R2DBC: en `infrastructure/entity/`, anotadas con `@Table`
- **Regla de beans**: ninguna clase del dominio (`domain/model/`, `domain/port/`, `domain/usecase/`) debe tener anotaciones de Spring (`@Component`, `@Service`, `@Repository`, etc.). Todos los beans del dominio se declaran explícitamente con `@Bean` en `config/ApplicationConfig.java`, recibiendo sus dependencias como parámetros del método. Solo la capa de infraestructura usa anotaciones de Spring directamente.
- No exponer entidades ni modelos de dominio directamente — mapear a DTOs en el controlador
- Manejo de errores con operadores reactivos: `onErrorResume`, `onErrorMap`
- Respuestas HTTP: `ResponseEntity<Mono<T>>`
- Validación: Bean Validation (`@Valid`, `@NotNull`, etc.) solo en DTOs de entrada
- **Comentarios obligatorios** en cada clase y método no trivial:
  - Javadoc (`/** */`) en la declaración de cada clase explicando su responsabilidad dentro de la arquitectura
  - Javadoc en cada método público explicando qué hace, parámetros y valor de retorno
  - Comentarios inline (`//`) en bloques de lógica compleja o decisiones no obvias

---

## Configuración (application.properties)
- PostgreSQL local: `localhost:5432`, base de datos: `finanzas_db`
- CORS habilitado para `http://localhost:4200`
- JWT secret y expiración configurables por properties
- R2DBC datasource (no JPA — solo R2DBC)

---

## Gestión de SQL

Todo el SQL del proyecto está externalizado en archivos `.sql` dentro de `src/main/resources/`. Hay dos carpetas con propósitos distintos:

### `resources/db/` — DDL y datos semilla (inicialización de BD)
Contiene los scripts que crean las tablas e insertan datos iniciales. Los ejecuta **Docker** al construir la imagen de PostgreSQL (copiados a `/docker-entrypoint-initdb.d/`). **Spring no los carga** — la app asume que las tablas ya existen al arrancar. Estos archivos sirven también como documentación del schema y para migraciones a otra BD.

| Archivo | Contenido |
|---------|-----------|
| `init.sql` | Script combinado: schema completo + seed data (lo ejecuta Docker al arrancar) |
| `01_schema.sql` | **Todas** las tablas, índices, constraints y FKs (referencia/documentacion) |
| `02_seed_data.sql` | Datos semilla: tipos de transacción, categorías, usuarios, transacciones de ejemplo |

**Reglas:**
- Toda sentencia `CREATE TABLE`, `ALTER TABLE`, `CREATE INDEX` va en `01_schema.sql`
- Toda sentencia `INSERT` de datos iniciales/semilla va en `02_seed_data.sql`
- Usar `IF NOT EXISTS` / `ON CONFLICT DO NOTHING` para que sean idempotentes
- Si se agrega una nueva tabla, agregarla en `01_schema.sql` respetando el orden de dependencias (FKs)

### `resources/sql/{modulo}/` — Queries de la aplicación (por módulo y método)
Contiene los queries SQL que usan los adaptadores de salida (`adapter/out/`). Cada archivo corresponde a **un método** del adaptador y se nombra de forma descriptiva en español.

**Estructura actual:**
```
resources/sql/
├── auth/
│   ├── buscar_usuario_por_email.sql
│   ├── registrar_usuario.sql
│   └── verificar_existencia_email.sql
└── transactions/
    ├── obtener_transaccion_por_id.sql
    ├── listar_transacciones_paginadas.sql       (filtros dinamicos + LIMIT/OFFSET)
    ├── contar_transacciones_filtradas.sql        (COUNT para paginacion)
    ├── obtener_balance_total.sql                 (SUM con CASE income/expense)
    ├── obtener_ingresos_mensuales.sql            (SUM WHERE type=income, mes/anio)
    ├── obtener_gastos_mensuales.sql              (SUM WHERE type=expense, mes/anio)
    ├── registrar_transaccion.sql
    ├── actualizar_transaccion.sql                (UPDATE seguro por userId)
    ├── eliminar_transaccion_por_id_y_usuario.sql (DELETE seguro por userId)
    └── verificar_existencia_transaccion.sql
```

**Reglas:**
- Un archivo `.sql` por cada método del adaptador que ejecute SQL
- Nombre del archivo: verbo descriptivo en español + entidad + filtro (ej. `listar_transacciones_por_usuario_y_tipo.sql`)
- Carpeta: `sql/{nombre_del_modulo}/` (ej. `sql/transactions/`, `sql/accounts/`, `sql/budgets/`)
- Los queries usan parámetros con nombre: `:id`, `:userId`, `:startDate`, etc.
- Cada archivo inicia con un comentario `--` describiendo qué hace el query
- **Nunca** escribir SQL directamente en las clases Java — siempre en archivo `.sql`

### `SqlQueryLoader` — Cargador de queries
La clase `config/SqlQueryLoader.java` carga los archivos `.sql` del classpath y los cachea en memoria. Los adaptadores lo inyectan así:

```java
private final SqlQueryLoader sqlQueryLoader;

// En cualquier método:
String sql = sqlQueryLoader.load("transactions/obtener_transaccion_por_id");
databaseClient.sql(sql).bind("id", transactionId)...
```

- El path es relativo a `resources/sql/`, sin extensión `.sql`
- Se cachea tras la primera lectura (no hay I/O repetitivo)

### Convención para nuevos módulos
Al crear un nuevo módulo con acceso a base de datos:
1. Agregar las tablas en `db/01_schema.sql`
2. Agregar datos semilla en `db/02_seed_data.sql` (si aplica)
3. Crear la carpeta `resources/sql/{modulo}/`
4. Crear un archivo `.sql` por cada método del adaptador
5. En el adaptador, inyectar `SqlQueryLoader` y usar `DatabaseClient` con los queries cargados

---

## Lo que NO se usa
- No OAuth2 / Azure AD
- No Spring MVC
- No Maven
- No JPA/Hibernate
- No frontend en este repositorio

---

## Integración con el Frontend Angular
El frontend solo cambia una línea en `app.config.ts`:
```typescript
{ provide: AUTH_PORT, useClass: JavaAuthAdapter }
```
El `JavaAuthAdapter` llama:
- `login(credentials)` → `POST /api/auth/login`
- `register(credentials)` → `POST /api/auth/register`

---

## Módulos Implementados

### auth (com.finanzas.personales.finanzas.auth)
- Login y registro con JWT
- BCrypt para contraseñas
- Tablas: `users`

### transactions (com.finanzas.personales.finanzas.transacciones)
- CRUD de transacciones (crear, listar paginado, actualizar, eliminar)
- Resumen financiero (balance total, ingresos/gastos mensuales, ahorro)
- Tablas maestras: `transaction_types` (income/expense), `categories` (12 categorías)
- Tabla principal: `transactions` con FK a `users`, `categories`, `transaction_types`
- Los queries SQL hacen JOIN para resolver nombres de categoría/tipo
- El frontend recibe strings lowercase (`"food"`, `"income"`) — el backend convierte a/desde enums
- Savings goal por defecto: 3000.00 (configurable en `GetSummaryUseCase`)

---

## Dominio Futuro (diseñar para escalar)
El sistema crecerá con:
- **Presupuestos**: límites de gasto por categoría y período
- **Cuentas**: cuentas bancarias o de efectivo del usuario
- **Reportes**: resúmenes y estadísticas por período

Cada módulo seguirá la misma arquitectura hexagonal: `domain/` (model + port + usecase) e `infrastructure/` (adapter/in, adapter/out, entity, dto).

---

## Testing

### Stack de pruebas (fijo)
- **JUnit 5** (`junit-jupiter`) — framework base de pruebas
- **Mockito** (`mockito-core`, `mockito-junit-jupiter`) — mocks y stubs
- **StepVerifier** (`reactor-test`) — verificación de flujos reactivos `Mono`/`Flux`
- **WebTestClient** — pruebas de integración de endpoints HTTP reactivos

### Tipos de prueba
| Tipo | Qué testear | Herramientas |
|------|-------------|--------------|
| Unitaria | Use cases del dominio | JUnit 5 + Mockito + StepVerifier |
| Unitaria | Controladores | JUnit 5 + Mockito + WebTestClient |
| Integración | Endpoints completos | `@SpringBootTest` + WebTestClient |

### Convenciones de tests
- Nombre de clase: sufijo `Test` (ej. `LoginUseCaseTest`, `AuthControllerTest`)
- Nombre de método: patrón `should_[resultado]_when_[condición]`
- Mockear los ports del dominio con `@Mock` / `@MockitoBean` — nunca la base de datos real en unitarios
- Verificar flujos reactivos siempre con `StepVerifier.create(...).expectNext(...).verifyComplete()`
- Un test por escenario: caso feliz, error esperado (credenciales inválidas, email duplicado, etc.)
- Comentario Javadoc en cada método de test explicando el escenario que cubre

---

## Artefactos del Proyecto
1. `build.gradle` con todas las dependencias (Groovy DSL)
2. `settings.gradle` con el nombre del proyecto
3. `application.properties` configurado (puerto 9000, R2DBC, JWT, CORS)
4. Todas las clases Java con código funcional y Lombok
5. Scripts SQL: `init.sql` (Docker), `01_schema.sql`, `02_seed_data.sql`, queries en `sql/`
6. Docker: `docker-compose.yml`, `docker/postgres/Dockerfile`, `docker/app/Dockerfile`
7. Tests unitarios: use cases, adapters, controllers

---

## Diagramas Mermaid

Cuando se genere un diagrama Mermaid, seguir estas reglas obligatorias para garantizar compatibilidad:

### Reglas
- Usar `graph TD` o `flowchart TD` — nunca `flowchart TB` con subgraphs complejos
- **Sin subgraphs anidados** — cada subgraph al mismo nivel, nunca uno dentro de otro
- **Sin comillas en el nombre del subgraph** — `subgraph CONFIG` no `subgraph CONFIG["config"]`
- **Sin `\n` dentro de etiquetas de nodos** — una sola línea por etiqueta
- **Sin caracteres especiales** en etiquetas: no `←`, `·`, `&lt;`, `&gt;`, `@`, `/` dentro de `[]`
- Usar `%%` para comentarios y separar secciones
- Etiquetas de nodos con texto plano ASCII únicamente

### Plantilla base
```
graph TD
    NODO[Nombre simple]

    subgraph GRUPO
        A[ComponenteA]
        B[ComponenteB]
    end

    A --> B
    A -.->|opcional| B
```

---

## Auto-Actualización de este Archivo

Este archivo (`copilot-instructions.md`) es la fuente de verdad del proyecto para Copilot. **Debe mantenerse sincronizado con el estado real del código.** Cada vez que se realice un cambio significativo en el proyecto, Copilot debe proponer la actualización correspondiente en este archivo.

### Cuándo actualizar
- **Nuevo módulo**: agregar en "Módulos Implementados" con su paquete, responsabilidades y tablas
- **Nuevo endpoint**: agregar en "Contrato de API" con request/response shapes exactos
- **Nueva tabla**: agregar en `01_schema.sql` y documentar en el módulo correspondiente
- **Nuevos archivos SQL**: agregar en el árbol de `resources/sql/` dentro de "Gestión de SQL"
- **Nueva dependencia**: agregar en "Stack Tecnológico" si es relevante
- **Nuevo use case**: actualizar el árbol de "Arquitectura Hexagonal" del módulo
- **Cambio en DTOs o modelos**: actualizar los shapes en "Contrato de API" y/o "Modelos del Frontend"
- **Nuevo usuario semilla**: agregar en la tabla de "Usuarios Iniciales"
- **Cambio en configuración**: actualizar "Configuración (application.properties)"
- **Nueva convención o regla**: agregar en "Convenciones de Código" o sección pertinente

### Cómo actualizar
1. Al finalizar la implementación de un cambio significativo, revisar qué secciones de este archivo se ven afectadas
2. Proponer la edición concreta (no reescribir todo — solo agregar/modificar lo necesario)
3. Mantener el mismo formato, estilo y nivel de detalle que las secciones existentes
4. Si se agrega un módulo nuevo, incluir: paquete completo, árbol de clases, endpoints, tablas y archivos SQL

### Qué NO actualizar automáticamente
- No modificar "Lo que NO se usa" sin confirmación del usuario
- No eliminar secciones existentes — solo agregar o editar
- No cambiar las reglas de "Convenciones de Código" sin instrucción explícita del usuario

