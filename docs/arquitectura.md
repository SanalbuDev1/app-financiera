flowchart TD
    FE[Frontend / JavaAuthAdapter]

    %% CONFIG
    subgraph CONFIG
        APP_CFG[ApplicationConfig]
        CORS[CorsConfig]
    end

    %% SECURITY
    subgraph SECURITY
        JWT_SVC[JwtService]
        JWT_FILTER[JwtAuthFilter]
        SEC_CFG[SecurityConfig]
    end

    %% DOMAIN
    subgraph DOMAIN_MODEL
        USER[User]
        ROLE[UserRole]
    end

    subgraph DOMAIN_PORT
        REPO_PORT[UserRepositoryPort]
    end

    subgraph DOMAIN_USECASE
        LOGIN_UC[LoginUseCase]
        REG_UC[RegisterUseCase]
        UNAUTH_EX[UnauthorizedException]
        EMAIL_EX[EmailAlreadyExistsException]
    end

    %% INFRASTRUCTURE
    subgraph INFRA_IN
        CTRL[AuthController]
    end

    subgraph INFRA_OUT
        R2DBC[R2dbcUserAdapter]
    end

    subgraph INFRA_ENTITY
        USER_ENTITY[UserEntity]
    end

    subgraph INFRA_DTO
        LOGIN_REQ[LoginRequest]
        REG_REQ[RegisterRequest]
        USER_RESP[UserResponse]
    end

    DB[(PostgreSQL)]

    %% FLOW
    FE -->|HTTP POST| CTRL

    CTRL --> LOGIN_UC
    CTRL --> REG_UC
    CTRL --> JWT_SVC

    APP_CFG --> LOGIN_UC
    APP_CFG --> REG_UC

    LOGIN_UC --> REPO_PORT
    REG_UC --> REPO_PORT

    LOGIN_UC -.->|throws| UNAUTH_EX
    REG_UC -.->|throws| EMAIL_EX

    R2DBC -->|implements| REPO_PORT
    R2DBC --> USER_ENTITY
    R2DBC --> DB

    JWT_FILTER --> JWT_SVC
    SEC_CFG --> JWT_FILTER