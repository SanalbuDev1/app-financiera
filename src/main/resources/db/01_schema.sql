-- =============================================================
-- Schema completo de la base de datos: finanzas_db
-- Contiene todas las tablas, indices y constraints
-- Motor: PostgreSQL
-- =============================================================

-- =============================================================
-- Tabla: users
-- =============================================================
CREATE TABLE IF NOT EXISTS users (
    id          VARCHAR(36)  PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(10)  NOT NULL DEFAULT 'USER'
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- =============================================================
-- Tabla maestra: transaction_types (income / expense)
-- =============================================================
CREATE TABLE IF NOT EXISTS transaction_types (
    id          VARCHAR(36)  PRIMARY KEY,
    name        VARCHAR(20)  NOT NULL UNIQUE,
    description VARCHAR(100)
);

-- =============================================================
-- Tabla maestra: categories
-- =============================================================
CREATE TABLE IF NOT EXISTS categories (
    id          VARCHAR(36)   PRIMARY KEY,
    name        VARCHAR(50)   NOT NULL UNIQUE,
    description VARCHAR(255),
    icon        VARCHAR(50),
    active      BOOLEAN       NOT NULL DEFAULT TRUE
);

-- =============================================================
-- Tabla: transactions (con FK a tablas maestras)
-- =============================================================
CREATE TABLE IF NOT EXISTS transactions (
    id                VARCHAR(36)    PRIMARY KEY,
    user_id           VARCHAR(36)    NOT NULL REFERENCES users(id),
    description       VARCHAR(255)   NOT NULL,
    amount            DECIMAL(12,2)  NOT NULL CHECK (amount > 0),
    category_id       VARCHAR(36)    NOT NULL REFERENCES categories(id),
    type_id           VARCHAR(36)    NOT NULL REFERENCES transaction_types(id),
    transaction_date  DATE           NOT NULL,
    notes             VARCHAR(500),
    created_at        TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions (user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions (user_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions (user_id, type_id);
CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions (user_id, category_id);
