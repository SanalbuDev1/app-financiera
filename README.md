# Finanzas Personales — Backend

Backend de finanzas personales construido con **Spring Boot 4.0.5 + Java 25 + WebFlux + R2DBC**. Se despliega automáticamente en **Azure Container Apps** mediante GitHub Actions al hacer push a `main`.

---

## Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4.0.5 — WebFlux (reactivo) |
| Base de datos | PostgreSQL 16 — acceso reactivo con R2DBC |
| Seguridad | Spring Security + JWT stateless |
| Build | Gradle (Groovy DSL) |
| Contenedor | Docker |
| API Docs | springdoc-openapi — Swagger UI en `/swagger-ui.html` |

---

## Infraestructura en Azure

| Recurso | Nombre | Región | SKU / Tier |
|---------|--------|--------|------------|
| Resource Group | `rg-finanzas` | East US | — |
| Container Registry | `finanzasacr` | East US | Basic |
| Container Apps Environment | `finanzas-env` | East US | Consumption |
| Container App (backend) | `finanzas-app` | East US | 0.25 CPU / 0.5 GB RAM — min 0 réplicas |
| PostgreSQL Flexible Server | `finanzas-pgserver` | Canada Central | Burstable B1ms |
| Static Web App (frontend) | `finanzas-frontend` | Global | Free |

### URLs de producción

| Servicio | URL |
|----------|-----|
| Backend API | https://finanzas-app.wittywave-bcfc6077.eastus.azurecontainerapps.io |
| Swagger UI | https://finanzas-app.wittywave-bcfc6077.eastus.azurecontainerapps.io/swagger-ui.html |
| Frontend | https://jolly-cliff-02e08840f.7.azurestaticapps.net |
| Base de datos | finanzas-pgserver.postgres.database.azure.com:5432 |

### Notas de costos

- **Container App**: se apaga con 0 tráfico (`min-replicas 0`) — costo $0 en reposo
- **PostgreSQL B1ms**: gratis los primeros 12 meses con cuenta free de Azure (750 hrs/mes, 32 GB storage)
- **Static Web App**: tier Free permanente

---

## Deploy Automático (CI/CD)

El archivo `.github/workflows/deploy-backend.yml` se dispara automáticamente cuando se hace `push` a `main` en rutas que afectan el backend (`src/**`, `build.gradle`, `docker/app/**`, etc.).

### Flujo completo

```
git push origin main
       │
       ▼
GitHub Actions (ubuntu-latest)
       │
       ├── 📥 Checkout código
       │       └── actions/checkout@v4
       │
       ├── 🔐 Azure Login (Service Principal)
       │       └── azure/login@v2  ← usa AZURE_CREDENTIALS secret
       │
       ├── 🐳 Login to ACR
       │       └── azure/docker-login@v2  ← finanzasacr.azurecr.io
       │
       ├── 🏗️ Build and push Docker image
       │       ├── docker build -f docker/app/Dockerfile
       │       ├── docker push finanzasacr.azurecr.io/finanzas-app:<sha>
       │       └── docker push finanzasacr.azurecr.io/finanzas-app:latest
       │
       └── 🚀 Deploy to Container Apps
               └── azure/container-apps-deploy-action@v2
                       ├── image: finanzas-app:<sha>
                       └── env vars inyectadas desde GitHub Secrets
```

### Hacer un deploy manual

Desde la UI de GitHub: `Actions → Deploy Backend → Run workflow → main`.

O desde la terminal:

```bash
git add .
git commit -m "descripción del cambio"
git push origin main
# El workflow arranca automáticamente en ~30 segundos
```

### Ver logs en tiempo real

```powershell
az containerapp logs show --name finanzas-app --resource-group rg-finanzas --follow
```

### Variables de entorno inyectadas en el deploy

El workflow inyecta estas variables en el Container App desde los GitHub Secrets:

| Variable | Fuente |
|----------|--------|
| `SPRING_PROFILES_ACTIVE` | hardcoded: `desarrollo` |
| `SPRING_R2DBC_URL` | secret `SPRING_R2DBC_URL` |
| `SPRING_R2DBC_USERNAME` | secret `SPRING_R2DBC_USERNAME` |
| `SPRING_R2DBC_PASSWORD` | secret ref: `db-password` |
| `APP_JWT_SECRET` | secret ref: `jwt-secret` |
| `APP_CORS_ALLOWED_ORIGINS` | secret `APP_CORS_ALLOWED_ORIGINS` |

---

## Levantar localmente

### Requisitos

- Docker Desktop
- Java 25
- Gradle

### Iniciar

```bash
# Levantar PostgreSQL local
docker compose up -d

# Compilar y correr
./gradlew bootRun
```

El servidor queda disponible en `http://localhost:9000`.

### Swagger UI local

```
http://localhost:9000/swagger-ui.html
```

---

## Estructura del Proyecto

```
src/main/java/com/finanzas/personales/finanzas/
├── auth/               # Login, registro, JWT
├── transacciones/      # CRUD transacciones, resumen financiero
├── security/           # JwtAuthFilter, SecurityConfig, JwtService
├── health/             # GET /api/health
└── config/             # Beans, CORS, R2DBC, SqlQueryLoader

src/main/resources/
├── db/                 # DDL (01_schema.sql) + seed data (02_seed_data.sql)
└── sql/                # Queries SQL por módulo (auth/, transactions/)
```

---

## Crear infraestructura desde cero

Si necesitás recrear toda la infraestructura en Azure, ejecutar los scripts en orden:

```powershell
.\scripts\00-login-azure.ps1          # Login en Azure
.\scripts\01-resource-group.ps1       # Resource Group
.\scripts\02-container-registry.ps1   # ACR
.\scripts\03-container-env.ps1        # Container Apps Environment
.\scripts\04-container-app.ps1        # Container App (backend)
.\scripts\05-static-web-app.ps1       # Static Web App (frontend)
.\scripts\06-service-principal.ps1    # Service Principal para CI/CD
.\scripts\07-azure-postgresql.ps1     # PostgreSQL Flexible Server
```

Luego configurar los **GitHub Secrets** según la guía en [`docs/deploy-azure.md`](docs/deploy-azure.md).
