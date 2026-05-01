# =============================================
# Imagen PostgreSQL para finanzas_db
# Base: postgres:16-alpine (ligera y estable)
# =============================================
FROM postgres:16-alpine

# Variables de entorno del contenedor
ENV POSTGRES_DB=finanzas_db
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres

# Copiar los scripts de inicialización al directorio que PostgreSQL
# ejecuta automáticamente al arrancar por primera vez.
# El contexto de build debe ser la raíz del proyecto (ver docker-compose.yml).
# Los archivos se ejecutan en orden alfabético → prefijos numéricos garantizan el orden.
COPY src/main/resources/db/*.sql /docker-entrypoint-initdb.d/

# Puerto estándar de PostgreSQL
EXPOSE 5432
