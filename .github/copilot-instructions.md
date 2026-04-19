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
- **Gradle con Kotlin DSL** (`build.gradle.kts`, **NO** Maven)
- Paquete raíz: `com.finanzas.personales.finanzas`

---

## Contrato de API (lo que espera el frontend Angular)

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
├── FinanzasApplication.java
│
├── auth/
│   ├── domain/
│   │   ├── model/         User.java, UserRole.java (enum)          ← entidades puras, sin frameworks
│   │   ├── port/          UserRepositoryPort.java                  ← puerto de salida (interface)
│   │   └── usecase/       LoginUseCase.java, RegisterUseCase.java  ← casos de uso (lógica de negocio)
│   │
│   └── infrastructure/
│       ├── adapter/
│       │   ├── in/        AuthController.java   (@RestController, /api/auth)   ← adaptador de entrada
│       │   └── out/       R2dbcUserAdapter.java  (implements UserRepositoryPort) ← adaptador de salida
│       ├── entity/        UserEntity.java        (@Table, Lombok)               ← entidad R2DBC
│       └── dto/           LoginRequest.java, RegisterRequest.java, UserResponse.java
│
├── security/
│   ├── JwtService.java        (generar y validar JWT)
│   ├── SecurityConfig.java    (SecurityWebFilterChain — WebFlux)
│   └── JwtAuthFilter.java     (WebFilter reactivo)
└── config/
    └── CorsConfig.java        (CORS para http://localhost:4200)
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

## Dominio Futuro (diseñar para escalar)
Además del módulo `auth`, el sistema crecerá con:
- **Transacciones**: ingresos y gastos (fecha, monto, categoría, descripción)
- **Categorías**: clasificación de transacciones
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

## Artefactos a Generar
1. `build.gradle.kts` con todas las dependencias necesarias
2. `settings.gradle.kts` con el nombre del proyecto
3. `application.properties` configurado (con soporte a perfiles dev/prod)
4. Todas las clases Java con código funcional y Lombok
5. Script SQL inicial para crear la tabla `users` en PostgreSQL
6. Test de integración con `WebTestClient` para login y register

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

