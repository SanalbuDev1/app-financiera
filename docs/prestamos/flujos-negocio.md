# Flujos de Negocio

## 1. Crear una nueva deuda

### Flujo principal

```
┌─────────────────────────────────────────────────────────────────────┐
│                     CREAR NUEVA DEUDA                               │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Usuario ingresa datos:                                             │
│  - Tipo de deuda (tarjeta, préstamo, vehículo, etc.)               │
│  - Acreedor (Bancolombia, Papá, etc.)                              │
│  - Monto original                                                   │
│  - Tasa de interés (mensual o anual)                               │
│  - Número de cuotas                                                 │
│  - Frecuencia de pago (mensual/quincenal)                          │
│  - Fecha de inicio                                                  │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Sistema calcula:                                                   │
│  1. Convierte tasa al período correspondiente                      │
│  2. Calcula cuota fija (fórmula francesa)                          │
│  3. Genera tabla de amortización completa                          │
│  4. Calcula total de intereses                                     │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Sistema guarda:                                                    │
│  - Registro en tabla `debts`                                       │
│  - Todas las cuotas en tabla `debt_schedule`                       │
│  - Estado inicial: 'active'                                        │
│  - Próxima fecha de pago calculada                                 │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Usuario ve:                                                        │
│  - Resumen de la deuda                                             │
│  - Cuota mensual a pagar                                           │
│  - Tabla de amortización                                           │
│  - Total de intereses proyectados                                  │
└─────────────────────────────────────────────────────────────────────┘
```

### Validaciones

| Campo | Validación |
|-------|------------|
| Monto | > 0 |
| Tasa | >= 0 y <= 100 |
| Cuotas | >= 1 y <= 360 (30 años) |
| Fecha inicio | No puede ser futura a más de 1 mes |

---

## 2. Registrar un pago regular

### Flujo principal

```
┌─────────────────────────────────────────────────────────────────────┐
│                    REGISTRAR PAGO REGULAR                           │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Usuario selecciona:                                                │
│  - La deuda a la que abona                                         │
│  - Fecha del pago                                                   │
│  - Monto pagado (usualmente = cuota fija)                          │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Sistema calcula automáticamente:                                   │
│  - Interés del período = Saldo × Tasa                              │
│  - Capital = Monto pagado - Interés                                │
│  - Nuevo saldo = Saldo anterior - Capital                          │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Sistema actualiza:                                                 │
│  - Registro en `debt_payments` con tipo 'regular'                  │
│  - Saldo actual en `debts`                                         │
│  - Cuotas restantes en `debts`                                     │
│  - Cuota correspondiente en `debt_schedule` → estado 'paid'        │
│  - Próxima fecha de pago                                           │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
              ┌───────────────┴───────────────┐
              │ ¿Saldo = 0?                   │
              └───────────────┬───────────────┘
                    │                 │
                   Sí                No
                    │                 │
                    ▼                 ▼
    ┌───────────────────────┐  ┌───────────────────────┐
    │ Estado → 'paid_off'   │  │ Continúa activa       │
    │ Notificar al usuario  │  │                       │
    └───────────────────────┘  └───────────────────────┘
```

---

## 3. Registrar un abono extraordinario

### Flujo principal

```
┌─────────────────────────────────────────────────────────────────────┐
│                  REGISTRAR ABONO EXTRAORDINARIO                     │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Usuario ingresa:                                                   │
│  - La deuda a la que abona                                         │
│  - Monto del abono extraordinario                                  │
│  - Estrategia:                                                      │
│    □ Reducir cuota (mantener plazo) ← Por defecto                  │
│    □ Reducir plazo (mantener cuota)                                │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Sistema procesa:                                                   │
│  1. Resta el abono del saldo actual                                │
│  2. Según la estrategia:                                           │
│     - Reducir cuota: Recalcula cuota con nuevo saldo               │
│     - Reducir plazo: Calcula nuevas cuotas restantes               │
│  3. Regenera tabla de amortización                                 │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Sistema guarda:                                                    │
│  - Registro en `debt_payments` con tipo 'extra'                    │
│  - Estrategia usada en `extra_payment_strategy`                    │
│  - Actualiza `debts` con nuevo saldo, cuota y/o plazo             │
│  - Elimina cuotas viejas de `debt_schedule`                       │
│  - Inserta nuevas cuotas proyectadas                              │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Usuario ve:                                                        │
│  - Confirmación del abono                                          │
│  - Nueva cuota (si aplicó reducir cuota)                           │
│  - Nuevo plazo (si aplicó reducir plazo)                           │
│  - Ahorro en intereses                                             │
│  - Nueva tabla de amortización                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Estrategias de abono extraordinario

#### Reducir cuota (por defecto)
```
Nuevo saldo = Saldo actual - Abono
Cuotas restantes = Sin cambio
Nueva cuota = Recalcular con fórmula francesa
```

**Beneficio:** Más liquidez mensual
**Ideal para:** Quien quiere reducir su gasto fijo mensual

#### Reducir plazo
```
Nuevo saldo = Saldo actual - Abono
Cuota = Sin cambio
Cuotas restantes = Recalcular (serán menos)
```

**Beneficio:** Pagar menos intereses totales
**Ideal para:** Quien quiere liberarse de la deuda más rápido

---

## 4. Alertas de vencimiento

### Flujo del job programado (diario)

```
┌─────────────────────────────────────────────────────────────────────┐
│              JOB DIARIO: VERIFICAR VENCIMIENTOS                     │
│                    (Ejecuta a las 8:00 AM)                          │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Por cada deuda activa:                                             │
│  - Obtener próxima fecha de pago                                   │
│  - Obtener preferencias del usuario (días antes, canal)            │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
              ┌───────────────┴───────────────┐
              │ ¿Próximo pago - días_antes    │
              │    <= fecha actual?           │
              └───────────────┬───────────────┘
                    │                 │
                   Sí                No
                    │                 │
                    ▼                 ▼
    ┌───────────────────────┐  ┌───────────────────────┐
    │ Enviar notificación   │  │ No hacer nada         │
    │ - Email si habilitado │  │                       │
    │ - WhatsApp si habilit │  │                       │
    └───────────────────────┘  └───────────────────────┘
                    │
                    ▼
    ┌───────────────────────────────────────────────────┐
    │  Registrar en `notification_logs`:                │
    │  - Canal usado                                    │
    │  - Estado (sent/failed)                           │
    │  - Timestamp                                      │
    └───────────────────────────────────────────────────┘
```

### Contenido de la notificación

```
📢 Recordatorio de pago

Hola [nombre],

Tu cuota de [descripción deuda] vence el [fecha].

💰 Monto a pagar: $[cuota]
🏦 Acreedor: [creditor]
📊 Saldo pendiente: $[saldo]

¡No olvides realizar tu pago a tiempo!
```

---

## 5. Consultar resumen de deudas

### Datos del resumen

```
┌─────────────────────────────────────────────────────────────────────┐
│                      RESUMEN DE DEUDAS                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Total deudas activas: 3                                           │
│  Saldo total pendiente: $25,500,000                                │
│  Pago mensual total: $2,150,000                                    │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ Próximos vencimientos:                                      │   │
│  │ • 20 May - Tarjeta Bancolombia - $450,000                  │   │
│  │ • 25 May - Crédito vehículo - $1,200,000                   │   │
│  │ • 01 Jun - Préstamo Papá - $500,000                        │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ Deudas por estado:                                          │   │
│  │ • Activas: 3                                                │   │
│  │ • Pagadas: 5                                                │   │
│  │ • En mora: 0                                                │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 6. Reglas de negocio

### Generales

| Regla | Descripción |
|-------|-------------|
| RN-001 | Una deuda solo puede pertenecer a un usuario |
| RN-002 | El monto de la deuda debe ser mayor a 0 |
| RN-003 | La tasa de interés no puede ser negativa |
| RN-004 | El número de cuotas debe ser al menos 1 |
| RN-005 | Una deuda pagada no puede recibir más pagos |

### Pagos

| Regla | Descripción |
|-------|-------------|
| RN-101 | Un pago no puede ser mayor al saldo pendiente + intereses del período |
| RN-102 | La fecha de pago no puede ser anterior a la fecha de inicio de la deuda |
| RN-103 | Un abono extraordinario debe ser al menos $1,000 |
| RN-104 | Al pagar la última cuota, la deuda cambia a estado 'paid_off' |

### Notificaciones

| Regla | Descripción |
|-------|-------------|
| RN-201 | Solo se envía una notificación por deuda por día |
| RN-202 | No se envían notificaciones de deudas pagadas |
| RN-203 | El usuario puede desactivar notificaciones por canal |
