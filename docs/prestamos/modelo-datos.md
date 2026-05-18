# Modelo de Datos

## Diagrama de Entidad-Relación

```
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│      users       │       │    debt_types    │       │payment_frequencies│
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ id (PK)          │       │ id (PK)          │       │ id (PK)          │
│ email            │       │ name             │       │ name             │
│ name             │       │ description      │       │ days_between     │
│ password         │       │ icon             │       └────────┬─────────┘
│ role             │       │ active           │                │
└────────┬─────────┘       └────────┬─────────┘                │
         │                          │                          │
         │ 1                        │ 1                        │ 1
         │                          │                          │
         │ *                        │ *                        │ *
         ▼                          ▼                          ▼
┌──────────────────────────────────────────────────────────────────────┐
│                              debts                                    │
├──────────────────────────────────────────────────────────────────────┤
│ id (PK)                                                               │
│ user_id (FK) ─────────────────────────────────────────────┐          │
│ debt_type_id (FK) ────────────────────────────────────────┤          │
│ frequency_id (FK) ────────────────────────────────────────┤          │
│ creditor                                                   │          │
│ description                                                │          │
│ original_amount                                            │          │
│ current_balance                                            │          │
│ interest_rate                                              │          │
│ interest_rate_type (monthly/annual)                        │          │
│ total_installments                                         │          │
│ remaining_installments                                     │          │
│ installment_amount                                         │          │
│ start_date                                                 │          │
│ next_payment_date                                          │          │
│ status (active/paid_off/defaulted)                         │          │
│ notes                                                      │          │
│ created_at                                                 │          │
└──────────────────────────────────────────────────────────────────────┘
         │                                    │
         │ 1                                  │ 1
         │                                    │
         │ *                                  │ *
         ▼                                    ▼
┌─────────────────────────┐      ┌─────────────────────────────────────┐
│     debt_payments       │      │          debt_schedule              │
├─────────────────────────┤      ├─────────────────────────────────────┤
│ id (PK)                 │      │ id (PK)                             │
│ debt_id (FK)            │      │ debt_id (FK)                        │
│ payment_date            │      │ installment_number                  │
│ total_amount            │      │ due_date                            │
│ principal_amount        │      │ principal_amount                    │
│ interest_amount         │      │ interest_amount                     │
│ payment_type            │      │ total_amount                        │
│ extra_payment_strategy  │      │ balance_after                       │
│ notes                   │      │ status (pending/paid/partial/overdue)│
│ created_at              │      │ created_at                          │
└─────────────────────────┘      └─────────────────────────────────────┘
```

## Tablas Detalladas

### users (existente)
Tabla de usuarios del sistema.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | VARCHAR(36) | UUID del usuario |
| email | VARCHAR(255) | Correo electrónico único |
| name | VARCHAR(255) | Nombre completo |
| password | VARCHAR(255) | Contraseña hasheada (BCrypt) |
| role | VARCHAR(10) | USER o ADMIN |

---

### debt_types
Catálogo de tipos de deuda.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | VARCHAR(36) | UUID del tipo |
| name | VARCHAR(50) | Nombre único (tarjeta_credito, prestamo_bancario, etc.) |
| description | VARCHAR(255) | Descripción legible |
| icon | VARCHAR(50) | Ícono para UI (Material Icons) |
| active | BOOLEAN | Si está activo para selección |

**Datos semilla:**
```sql
INSERT INTO debt_types (id, name, description, icon) VALUES
('debt-type-credit-card', 'tarjeta_credito', 'Tarjeta de crédito', 'credit_card'),
('debt-type-bank-loan', 'prestamo_bancario', 'Préstamo bancario', 'account_balance'),
('debt-type-vehicle', 'credito_vehiculo', 'Crédito de vehículo', 'directions_car'),
('debt-type-mortgage', 'hipoteca', 'Crédito hipotecario', 'home'),
('debt-type-informal', 'prestamo_informal', 'Préstamo informal (persona)', 'person'),
('debt-type-other', 'otro', 'Otro tipo de deuda', 'more_horiz');
```

---

### payment_frequencies
Catálogo de frecuencias de pago.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | VARCHAR(36) | UUID de la frecuencia |
| name | VARCHAR(20) | Nombre (mensual, quincenal) |
| days_between_payments | INT | Días entre pagos |

**Datos semilla:**
```sql
INSERT INTO payment_frequencies (id, name, days_between_payments) VALUES
('freq-monthly', 'mensual', 30),
('freq-biweekly', 'quincenal', 15);
```

---

### debts
Tabla principal de deudas.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | VARCHAR(36) | UUID de la deuda |
| user_id | VARCHAR(36) | FK a users |
| debt_type_id | VARCHAR(36) | FK a debt_types |
| frequency_id | VARCHAR(36) | FK a payment_frequencies |
| creditor | VARCHAR(100) | Nombre del acreedor |
| description | VARCHAR(255) | Descripción de la deuda |
| original_amount | DECIMAL(15,2) | Monto inicial |
| current_balance | DECIMAL(15,2) | Saldo actual |
| interest_rate | DECIMAL(6,4) | Tasa de interés (ej: 1.5000) |
| interest_rate_type | VARCHAR(10) | 'monthly' o 'annual' |
| total_installments | INT | Número total de cuotas |
| remaining_installments | INT | Cuotas pendientes |
| installment_amount | DECIMAL(15,2) | Valor de la cuota actual |
| start_date | DATE | Fecha de inicio |
| next_payment_date | DATE | Próxima fecha de pago |
| status | VARCHAR(20) | 'active', 'paid_off', 'defaulted' |
| notes | VARCHAR(500) | Notas adicionales |
| created_at | TIMESTAMP | Fecha de creación |

**Índices:**
```sql
CREATE INDEX idx_debts_user_id ON debts (user_id);
CREATE INDEX idx_debts_status ON debts (user_id, status);
CREATE INDEX idx_debts_next_payment ON debts (next_payment_date);
```

---

### debt_payments
Historial de pagos realizados.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | VARCHAR(36) | UUID del pago |
| debt_id | VARCHAR(36) | FK a debts |
| payment_date | DATE | Fecha del pago |
| total_amount | DECIMAL(15,2) | Monto total pagado |
| principal_amount | DECIMAL(15,2) | Abono a capital |
| interest_amount | DECIMAL(15,2) | Pago de intereses |
| payment_type | VARCHAR(20) | 'regular' o 'extra' |
| extra_payment_strategy | VARCHAR(20) | 'reduce_installment' o 'reduce_term' (solo para extras) |
| notes | VARCHAR(500) | Notas del pago |
| created_at | TIMESTAMP | Fecha de registro |

**Índices:**
```sql
CREATE INDEX idx_debt_payments_debt_id ON debt_payments (debt_id);
CREATE INDEX idx_debt_payments_date ON debt_payments (debt_id, payment_date);
```

---

### debt_schedule
Tabla de amortización (proyección de cuotas).

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | VARCHAR(36) | UUID del ítem |
| debt_id | VARCHAR(36) | FK a debts |
| installment_number | INT | Número de cuota (1, 2, 3...) |
| due_date | DATE | Fecha de vencimiento |
| principal_amount | DECIMAL(15,2) | Capital proyectado |
| interest_amount | DECIMAL(15,2) | Intereses proyectados |
| total_amount | DECIMAL(15,2) | Total de la cuota |
| balance_after | DECIMAL(15,2) | Saldo después de esta cuota |
| status | VARCHAR(20) | 'pending', 'paid', 'partial', 'overdue' |
| created_at | TIMESTAMP | Fecha de creación |

**Índices:**
```sql
CREATE INDEX idx_debt_schedule_debt_id ON debt_schedule (debt_id);
CREATE INDEX idx_debt_schedule_due_date ON debt_schedule (due_date, status);
```

---

### user_notification_preferences
Preferencias de notificación por usuario.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | VARCHAR(36) | UUID |
| user_id | VARCHAR(36) | FK a users (UNIQUE) |
| email_enabled | BOOLEAN | Notificar por email |
| whatsapp_enabled | BOOLEAN | Notificar por WhatsApp |
| whatsapp_number | VARCHAR(20) | Número de WhatsApp |
| days_before_alert | INT | Días antes para alertar |
| created_at | TIMESTAMP | Fecha de creación |

---

### notification_logs
Historial de notificaciones enviadas.

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | VARCHAR(36) | UUID |
| user_id | VARCHAR(36) | FK a users |
| debt_id | VARCHAR(36) | FK a debts |
| channel | VARCHAR(20) | 'email' o 'whatsapp' |
| status | VARCHAR(20) | 'sent', 'failed', 'pending' |
| message | TEXT | Contenido del mensaje |
| sent_at | TIMESTAMP | Fecha/hora de envío |
| created_at | TIMESTAMP | Fecha de creación |

**Índices:**
```sql
CREATE INDEX idx_notification_logs_user ON notification_logs (user_id);
CREATE INDEX idx_notification_logs_debt ON notification_logs (debt_id);
```

---

## Relaciones

| Relación | Tipo | Descripción |
|----------|------|-------------|
| users → debts | 1:N | Un usuario tiene muchas deudas |
| debt_types → debts | 1:N | Un tipo tiene muchas deudas |
| payment_frequencies → debts | 1:N | Una frecuencia aplica a muchas deudas |
| debts → debt_payments | 1:N | Una deuda tiene muchos pagos |
| debts → debt_schedule | 1:N | Una deuda tiene muchas cuotas proyectadas |
| users → user_notification_preferences | 1:1 | Un usuario tiene una config de notificaciones |
| users → notification_logs | 1:N | Un usuario tiene muchos logs |
| debts → notification_logs | 1:N | Una deuda genera muchos logs |

---

## Queries SQL comunes

### Obtener deudas activas de un usuario
```sql
SELECT d.*, dt.name as debt_type_name, pf.name as frequency_name
FROM debts d
JOIN debt_types dt ON d.debt_type_id = dt.id
JOIN payment_frequencies pf ON d.frequency_id = pf.id
WHERE d.user_id = :userId AND d.status = 'active'
ORDER BY d.next_payment_date ASC;
```

### Obtener tabla de amortización
```sql
SELECT * FROM debt_schedule
WHERE debt_id = :debtId
ORDER BY installment_number ASC;
```

### Resumen de deudas
```sql
SELECT
    COUNT(*) as total_debts,
    SUM(current_balance) as total_balance,
    SUM(installment_amount) as total_monthly_payment
FROM debts
WHERE user_id = :userId AND status = 'active';
```

### Próximos vencimientos
```sql
SELECT d.*, ds.due_date, ds.total_amount
FROM debts d
JOIN debt_schedule ds ON d.id = ds.debt_id
WHERE d.user_id = :userId
  AND d.status = 'active'
  AND ds.status = 'pending'
  AND ds.due_date <= CURRENT_DATE + INTERVAL '7 days'
ORDER BY ds.due_date ASC;
```
