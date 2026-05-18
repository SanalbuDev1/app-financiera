-- =============================================================
-- Script de inicializacion de la base de datos: finanzas_db
-- Motor: PostgreSQL
-- Combina: 01_schema.sql + 02_seed_data.sql
-- =============================================================

-- =============================================================
-- 1. SCHEMA — Tablas, indices, constraints
-- =============================================================

-- Tabla: users
CREATE TABLE IF NOT EXISTS users (
    id          VARCHAR(36)  PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(10)  NOT NULL DEFAULT 'USER'
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- Tabla maestra: transaction_types (income / expense)
CREATE TABLE IF NOT EXISTS transaction_types (
    id          VARCHAR(36)  PRIMARY KEY,
    name        VARCHAR(20)  NOT NULL UNIQUE,
    description VARCHAR(100)
);

-- Tabla maestra: categories
CREATE TABLE IF NOT EXISTS categories (
    id          VARCHAR(36)   PRIMARY KEY,
    name        VARCHAR(50)   NOT NULL UNIQUE,
    description VARCHAR(255),
    icon        VARCHAR(50),
    active      BOOLEAN       NOT NULL DEFAULT TRUE
);

-- Tabla: transactions (con FK a tablas maestras)
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
-- MODULO DE DEUDAS
-- =============================================================

CREATE TABLE IF NOT EXISTS debt_types (
    id          VARCHAR(36)  PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255),
    icon        VARCHAR(50),
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS payment_frequencies (
    id                    VARCHAR(36)  PRIMARY KEY,
    name                  VARCHAR(20)  NOT NULL UNIQUE,
    days_between_payments INT          NOT NULL
);

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
    next_payment_date      DATE,
    status                 VARCHAR(20)    NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'paid_off', 'defaulted')),
    notes                  VARCHAR(500),
    created_at             TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_debts_user_id ON debts (user_id);
CREATE INDEX IF NOT EXISTS idx_debts_status ON debts (user_id, status);
CREATE INDEX IF NOT EXISTS idx_debts_next_payment ON debts (next_payment_date);

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

-- =============================================================
-- 2. SEED DATA — Datos iniciales para dev/testing
-- =============================================================

-- Tipos de transaccion
INSERT INTO transaction_types (id, name, description)
VALUES
    ('type-income',  'income',  'Transaccion de ingreso'),
    ('type-expense', 'expense', 'Transaccion de gasto')
ON CONFLICT (name) DO NOTHING;

-- Categorias
INSERT INTO categories (id, name, description, icon)
VALUES
    ('cat-food',          'food',          'Alimentacion y comida',           'restaurant'),
    ('cat-transport',     'transport',     'Transporte y movilidad',          'directions_car'),
    ('cat-entertainment', 'entertainment', 'Entretenimiento y ocio',          'movie'),
    ('cat-health',        'health',        'Salud y gastos medicos',          'local_hospital'),
    ('cat-education',     'education',     'Educacion y formacion',           'school'),
    ('cat-shopping',      'shopping',      'Compras y articulos personales',  'shopping_cart'),
    ('cat-bills',         'bills',         'Servicios y facturas',            'receipt_long'),
    ('cat-salary',        'salary',        'Salario e ingresos laborales',    'account_balance'),
    ('cat-investment',    'investment',    'Inversiones y rendimientos',       'trending_up'),
    ('cat-freelance',     'freelance',     'Trabajos independientes',         'laptop'),
    ('cat-savings',       'savings',       'Ahorros',                         'savings'),
    ('cat-other',         'other',         'Otras categorias',                'more_horiz')
ON CONFLICT (name) DO NOTHING;

-- Usuarios iniciales (passwords BCrypt: admin123, user123)
INSERT INTO users (id, email, name, password, role)
VALUES
    (
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'admin@financiera.com',
        'Administrador',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'ADMIN'
    ),
    (
        'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'user@financiera.com',
        'Usuario',
        '$2a$10$TKh8H1.PfTuCBq7EeDOdIeF1EIMLWZ.MHXSf7EFkuFnFQvGMr5pYK',
        'USER'
    )
ON CONFLICT (email) DO NOTHING;

-- Transacciones de ejemplo para user@financiera.com (abril 2026)
INSERT INTO transactions (id, user_id, description, amount, category_id, type_id, transaction_date, notes) VALUES
-- Ingresos
('tx-001', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Salario quincenal 1',       2250.00, 'cat-salary',        'type-income',  '2026-04-01', 'Primera quincena'),
('tx-002', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Salario quincenal 2',       2250.00, 'cat-salary',        'type-income',  '2026-04-16', 'Segunda quincena'),
('tx-003', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Freelance diseno web',       800.00, 'cat-freelance',     'type-income',  '2026-04-05', 'Proyecto landing page'),
('tx-004', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Dividendos acciones',        350.00, 'cat-investment',    'type-income',  '2026-04-10', 'Rendimiento mensual'),
('tx-005', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Freelance consultoria',      500.00, 'cat-freelance',     'type-income',  '2026-04-18', 'Asesoria tecnica'),
('tx-006', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Venta articulos usados',     120.00, 'cat-other',         'type-income',  '2026-04-12', NULL),
-- Gastos: Facturas y servicios
('tx-007', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Arriendo mensual',         1200.00, 'cat-bills',         'type-expense', '2026-04-02', NULL),
('tx-008', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Administracion edificio',    150.00, 'cat-bills',         'type-expense', '2026-04-02', NULL),
-- Gastos: Alimentacion
('tx-009', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Mercado semanal 1',          320.00, 'cat-food',          'type-expense', '2026-04-03', NULL),
('tx-010', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Mercado semanal 2',          280.00, 'cat-food',          'type-expense', '2026-04-10', NULL),
('tx-011', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Mercado semanal 3',          310.00, 'cat-food',          'type-expense', '2026-04-17', NULL),
('tx-012', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Restaurante cena',            85.00, 'cat-food',          'type-expense', '2026-04-08', 'Cena con amigos'),
('tx-013', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Almuerzo trabajo',            35.00, 'cat-food',          'type-expense', '2026-04-14', NULL),
-- Gastos: Transporte
('tx-014', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Gasolina',                   120.00, 'cat-transport',     'type-expense', '2026-04-05', NULL),
('tx-015', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Uber viajes semana',          65.00, 'cat-transport',     'type-expense', '2026-04-07', NULL),
('tx-016', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Parqueadero mensual',         80.00, 'cat-transport',     'type-expense', '2026-04-01', NULL),
-- Gastos: Servicios
('tx-017', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Luz electrica',               85.00, 'cat-bills',         'type-expense', '2026-04-10', NULL),
('tx-018', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Agua',                        35.00, 'cat-bills',         'type-expense', '2026-04-11', NULL),
('tx-019', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Internet fibra',              65.00, 'cat-bills',         'type-expense', '2026-04-11', NULL),
('tx-020', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Plan celular',                45.00, 'cat-bills',         'type-expense', '2026-04-12', NULL),
-- Gastos: Entretenimiento
('tx-021', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Netflix',                     45.00, 'cat-entertainment', 'type-expense', '2026-04-04', NULL),
('tx-022', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Spotify',                     15.00, 'cat-entertainment', 'type-expense', '2026-04-04', NULL),
('tx-023', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Cine',                        30.00, 'cat-entertainment', 'type-expense', '2026-04-15', 'Pelicula estreno'),
-- Gastos: Salud
('tx-024', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Seguro salud',               200.00, 'cat-health',        'type-expense', '2026-04-06', NULL),
('tx-025', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Farmacia',                    45.00, 'cat-health',        'type-expense', '2026-04-13', NULL),
-- Gastos: Educacion
('tx-026', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Curso Angular Udemy',         50.00, 'cat-education',     'type-expense', '2026-04-09', 'Angular 21 avanzado'),
('tx-027', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Libro programacion',          35.00, 'cat-education',     'type-expense', '2026-04-14', NULL),
-- Gastos: Compras
('tx-028', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Ropa nueva',                 180.00, 'cat-shopping',      'type-expense', '2026-04-12', NULL),
('tx-029', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Articulos hogar',             95.00, 'cat-shopping',      'type-expense', '2026-04-19', NULL),
-- Gastos: Otros
('tx-030', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Regalo cumpleanos',           60.00, 'cat-other',         'type-expense', '2026-04-20', 'Cumple de Maria')
ON CONFLICT (id) DO NOTHING;

-- =============================================================
-- MODULO DE DEUDAS — Datos semilla
-- =============================================================

-- Tipos de deuda
INSERT INTO debt_types (id, name, description, icon, active)
VALUES
    ('debt-type-credit-card', 'tarjeta_credito',   'Tarjeta de credito',       'credit_card',     TRUE),
    ('debt-type-bank-loan',   'prestamo_bancario', 'Prestamo bancario',        'account_balance', TRUE),
    ('debt-type-vehicle',     'credito_vehiculo',  'Credito de vehiculo',      'directions_car',  TRUE),
    ('debt-type-mortgage',    'hipoteca',          'Credito hipotecario',      'home',            TRUE),
    ('debt-type-informal',    'prestamo_informal', 'Prestamo informal (persona)', 'person',       TRUE),
    ('debt-type-other',       'otro',              'Otro tipo de deuda',       'more_horiz',      TRUE)
ON CONFLICT (name) DO NOTHING;

-- Frecuencias de pago
INSERT INTO payment_frequencies (id, name, days_between_payments)
VALUES
    ('freq-monthly',   'mensual',   30),
    ('freq-biweekly',  'quincenal', 15)
ON CONFLICT (name) DO NOTHING;

-- Deudas de ejemplo para user@financiera.com
INSERT INTO debts (
    id, user_id, debt_type_id, frequency_id, creditor, description,
    original_amount, current_balance, interest_rate, interest_rate_type,
    total_installments, remaining_installments, installment_amount,
    start_date, next_payment_date, status, notes
) VALUES
    (
        'debt-001',
        'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'debt-type-bank-loan',
        'freq-monthly',
        'Bancolombia',
        'Prestamo personal',
        10000000.00, 7663241.06, 1.5, 'monthly',
        12, 9, 917351.85,
        '2026-01-01', '2026-06-01', 'active',
        'Prestamo para remodelacion'
    ),
    (
        'debt-002',
        'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'debt-type-credit-card',
        'freq-monthly',
        'Visa Davivienda',
        'Tarjeta de credito',
        2500000.00, 2500000.00, 2.5, 'monthly',
        24, 24, 141788.00,
        '2026-05-01', '2026-06-01', 'active',
        NULL
    )
ON CONFLICT (id) DO NOTHING;
