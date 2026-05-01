# =================================================================
# Imagen multi-stage para la aplicación Spring Boot — finanzas
# Stage 1 (builder): compila el JAR con Gradle
# Stage 2 (runtime): ejecuta solo el JAR en una JRE ligera
# =================================================================

# ---- Stage 1: Build ----
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Copiar primero los archivos de configuración de Gradle.
# Docker cachea esta capa; solo se invalida si cambian las dependencias.
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Dar permiso de ejecución al wrapper y pre-descargar dependencias
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon --quiet || true

# Copiar el código fuente y construir el JAR ejecutable
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copiar únicamente el JAR generado (imagen final más pequeña)
COPY --from=builder /app/build/libs/finanzas-0.0.1-SNAPSHOT.jar app.jar

# Puerto que expone Spring Boot
EXPOSE 9000

# Opciones de JVM recomendadas para contenedores:
#   -XX:+UseContainerSupport  → respeta los límites de memoria del contenedor
#   -XX:MaxRAMPercentage=75.0 → usa hasta el 75 % de la RAM asignada
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default}", "-jar", "app.jar"]
