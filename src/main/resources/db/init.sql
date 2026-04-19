-- =============================================================
-- Script de inicialización de la base de datos: finanzas_db
-- Motor: PostgreSQL
-- =============================================================

-- Crear el tipo ENUM para los roles de usuario
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('ADMIN', 'USER');
EXCEPTION
    WHEN duplicate_object THEN NULL; -- No lanzar error si ya existe
END $$;

-- Crear la tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id          VARCHAR(36)  PRIMARY KEY,              -- UUID generado por la aplicación
    email       VARCHAR(255) NOT NULL UNIQUE,          -- Email único del usuario
    name        VARCHAR(255) NOT NULL,                 -- Nombre completo
    password    VARCHAR(255) NOT NULL,                 -- Contraseña hasheada con BCrypt
    role        VARCHAR(10)  NOT NULL DEFAULT 'USER'   -- Rol: ADMIN o USER
);

-- Índice para búsquedas frecuentes por email (login)
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- =============================================================
-- Datos iniciales para dev/testing
-- Contraseñas hasheadas con BCrypt:
--   admin123 → $2a$10$...
--   user123  → $2a$10$...
-- =============================================================
INSERT INTO users (id, email, name, password, role)
VALUES
    (
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'admin@financiera.com',
        'Administrador',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- admin123
        'ADMIN'
    ),
    (
        'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'user@financiera.com',
        'Usuario',
        '$2a$10$TKh8H1.PfTuCBq7EeDOdIeF1EIMLWZ.MHXSf7EFkuFnFQvGMr5pYK',  -- user123
        'USER'
    )
ON CONFLICT (email) DO NOTHING; -- Evitar duplicados si se ejecuta más de una vez
