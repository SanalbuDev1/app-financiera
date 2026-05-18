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

-- =============================================================
-- MODULO DE DEUDAS / PRESTAMOS
-- =============================================================

-- =============================================================
-- Tabla maestra: debt_types (tipos de deuda)
-- =============================================================
CREATE TABLE IF NOT EXISTS debt_types (
    id          VARCHAR(36)  PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255),
    icon        VARCHAR(50),
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

-- =============================================================
-- Tabla maestra: payment_frequencies (frecuencias de pago)
-- =============================================================
CREATE TABLE IF NOT EXISTS payment_frequencies (
    id                    VARCHAR(36)  PRIMARY KEY,
    name                  VARCHAR(20)  NOT NULL UNIQUE,
    days_between_payments INT          NOT NULL
);

-- =============================================================
-- Tabla: debts (deudas principales)
-- =============================================================
CREATE TABLE IF NOT EXISTS debts (
    id                     VARCHAR(36)    PRIMARY KEY,
    user_id                VARCHAR(36)    NOT NULL REFERENCES users(id),
    debt_type_id           VARCHAR(36)    NOT NULL REFERENCES debt_types(id),
    frequency_id           VARCHAR(36)    NOT NULL REFERENCES payment_frequencies(id),
    creditor               VARCHAR(100)   NOT NULL,
    description            VARCHAR(255),
    original_amount        DECIMAL(15,2)  NOT NULL CHECK (original_amount > 0),
    current_balance        DECIMAL(15,2)  NOT NULL CHECK (current_balance >= 0),
    interest_rate          DECIMAL(6,4)   NOT NULL CHECK (interest_rate >= 0),
    interest_rate_type     VARCHAR(10)    NOT NULL CHECK (interest_rate_type IN ('monthly', 'annual')),
    total_installments     INT            NOT NULL CHECK (total_installments > 0),
    remaining_installments INT            NOT NULL CHECK (remaining_installments >= 0),
    installment_amount     DECIMAL(15,2)  NOT NULL CHECK (installment_amount > 0),
    start_date             DATE           NOT NULL,
    next_payment_date      DATE,                    -- NULL cuando la deuda está saldada (paid_off)
    status                 VARCHAR(20)    NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'paid_off', 'defaulted')),
    notes                  VARCHAR(500),
    created_at             TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_debts_user_id ON debts (user_id);
CREATE INDEX IF NOT EXISTS idx_debts_status ON debts (user_id, status);
CREATE INDEX IF NOT EXISTS idx_debts_next_payment ON debts (next_payment_date);

-- =============================================================
-- Tabla: debt_payments (historial de pagos)
-- =============================================================
CREATE TABLE IF NOT EXISTS debt_payments (
    id                     VARCHAR(36)    PRIMARY KEY,
    debt_id                VARCHAR(36)    NOT NULL REFERENCES debts(id) ON DELETE CASCADE,
    payment_date           DATE           NOT NULL,
    total_amount           DECIMAL(15,2)  NOT NULL CHECK (total_amount > 0),
    principal_amount       DECIMAL(15,2)  NOT NULL CHECK (principal_amount >= 0),
    interest_amount        DECIMAL(15,2)  NOT NULL CHECK (interest_amount >= 0),
    payment_type           VARCHAR(20)    NOT NULL CHECK (payment_type IN ('regular', 'extra')),
    extra_payment_strategy VARCHAR(20)    CHECK (extra_payment_strategy IN ('reduce_installment', 'reduce_term')),
    notes                  VARCHAR(500),
    created_at             TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_debt_payments_debt_id ON debt_payments (debt_id);
CREATE INDEX IF NOT EXISTS idx_debt_payments_date ON debt_payments (debt_id, payment_date);

-- =============================================================
-- Tabla: debt_schedule (tabla de amortizacion)
-- =============================================================
CREATE TABLE IF NOT EXISTS debt_schedule (
    id                 VARCHAR(36)    PRIMARY KEY,
    debt_id            VARCHAR(36)    NOT NULL REFERENCES debts(id) ON DELETE CASCADE,
    installment_number INT            NOT NULL CHECK (installment_number > 0),
    due_date           DATE           NOT NULL,
    principal_amount   DECIMAL(15,2)  NOT NULL CHECK (principal_amount >= 0),
    interest_amount    DECIMAL(15,2)  NOT NULL CHECK (interest_amount >= 0),
    total_amount       DECIMAL(15,2)  NOT NULL CHECK (total_amount > 0),
    balance_after      DECIMAL(15,2)  NOT NULL CHECK (balance_after >= 0),
    status             VARCHAR(20)    NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'paid', 'partial', 'overdue')),
    created_at         TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_debt_schedule_debt_id ON debt_schedule (debt_id);
CREATE INDEX IF NOT EXISTS idx_debt_schedule_due_date ON debt_schedule (due_date, status);
CREATE UNIQUE INDEX IF NOT EXISTS idx_debt_schedule_unique ON debt_schedule (debt_id, installment_number);
