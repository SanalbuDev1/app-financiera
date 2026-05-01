-- =============================================================
-- Datos semilla para dev/testing
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

-- =============================================================
-- Transacciones de ejemplo para user@financiera.com (abril 2026)
-- =============================================================
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
