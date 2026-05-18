# Prompt para el frontend Angular — Módulo de Deudas

Implementa el módulo completo de Deudas en Angular 21 (standalone components, signals, SSR).
El proyecto ya tiene autenticación JWT funcionando, un AuthService que guarda el token,
y un HttpClient configurado con interceptor que agrega el header Authorization: Bearer <token>.

---

## STACK
- Angular 21, standalone components, signals
- Tailwind CSS (mismo estilo visual que el módulo de transacciones existente)
- Angular Router con lazy loading
- HttpClient con interceptor JWT ya configurado

---

## MODELOS TypeScript

```typescript
export interface Debt {
  id: string;
  userId: string;
  debtTypeId: string;
  debtTypeName: string;
  frequencyId: string;
  frequencyName: string;
  creditor: string;
  description: string;
  originalAmount: number;
  currentBalance: number;
  interestRate: number;
  interestRateType: 'monthly' | 'annual';
  totalInstallments: number;
  remainingInstallments: number;
  installmentAmount: number;
  startDate: string;       // 'YYYY-MM-DD'
  nextPaymentDate: string; // 'YYYY-MM-DD'
  status: 'active' | 'paid_off' | 'defaulted';
  notes?: string;
  createdAt: string;
  progressPercentage: number;
}

export interface DebtScheduleItem {
  id: string;
  debtId: string;
  installmentNumber: number;
  dueDate: string;         // 'YYYY-MM-DD'
  principalAmount: number;
  interestAmount: number;
  totalAmount: number;
  balanceAfter: number;
  status: 'pending' | 'paid' | 'partial' | 'overdue';
  createdAt: string;
}

export interface DebtPayment {
  id: string;
  debtId: string;
  paymentDate: string;
  totalAmount: number;
  principalAmount: number;
  interestAmount: number;
  paymentType: 'regular' | 'extra';
  extraPaymentStrategy?: 'reduce_installment' | 'reduce_term';
  notes?: string;
  createdAt: string;
}

export interface DebtSummary {
  totalDebts: number;
  totalBalance: number;
  totalOriginalAmount: number;
  totalMonthlyPayment: number;
  totalPendingInterest: number;
  averageProgress: number;
}

export interface DebtDetail {
  debt: Debt;
  schedule: DebtScheduleItem[];
}

export interface CreateDebtRequest {
  creditor: string;
  description: string;
  debtTypeId: string;
  frequencyId: string;
  originalAmount: number;
  interestRate: number;
  interestRateType: 'monthly' | 'annual';
  totalInstallments: number;
  startDate: string;       // 'YYYY-MM-DD'
  notes?: string;
}

export interface RegisterPaymentRequest {
  paymentDate?: string;    // 'YYYY-MM-DD' — opcional, default hoy
  totalAmount: number;     // ignorado en pagos regular, obligatorio en extra
  paymentType: 'regular' | 'extra';
  extraPaymentStrategy?: 'reduce_installment' | 'reduce_term'; // solo para extra
  notes?: string;
}
```

---

## API ENDPOINTS (base: http://localhost:9000)

Todos requieren `Authorization: Bearer <token>` excepto donde se indique.

### Deudas
| Método | URL | Body | Response |
|--------|-----|------|----------|
| GET | /api/debts | — | Debt[] |
| GET | /api/debts?status=active | — | Debt[] |
| GET | /api/debts/{id} | — | DebtDetail { debt, schedule[] } |
| POST | /api/debts | CreateDebtRequest | 201 Debt |
| PUT | /api/debts/{id} | { creditor, description, notes } | 200 Debt |
| DELETE | /api/debts/{id} | — | 204 |
| GET | /api/debts/summary | — | DebtSummary |

### Pagos y cronograma
| Método | URL | Body | Response |
|--------|-----|------|----------|
| POST | /api/debts/{id}/payments | RegisterPaymentRequest | 201 DebtPayment |
| GET | /api/debts/{id}/schedule | — | DebtScheduleItem[] |

### Datos maestros (hardcoded en el frontend)
Los tipos de deuda disponibles:
| debtTypeId | Nombre visible |
|------------|---------------|
| debt-type-credit-card | Tarjeta de crédito |
| debt-type-bank-loan | Préstamo bancario |
| debt-type-vehicle | Crédito vehículo |
| debt-type-mortgage | Hipoteca |
| debt-type-informal | Préstamo informal |
| debt-type-other | Otro |

Las frecuencias disponibles:
| frequencyId | Nombre visible |
|-------------|---------------|
| freq-monthly | Mensual (30 días) |
| freq-biweekly | Quincenal (15 días) |

---

## VISTAS A IMPLEMENTAR

### 1. Página resumen de deudas `/debts`
- Tarjetas de resumen arriba (DebtSummary): total adeudado, cuota mensual total,
  intereses pendientes, progreso promedio (barra)
- Lista de deudas: cada tarjeta muestra creditor, description, currentBalance,
  installmentAmount, nextPaymentDate, progressPercentage (barra de progreso),
  status badge (active=verde, paid_off=gris, defaulted=rojo)
- Filtro por status (active / paid_off / defaulted / todos)
- Botón "Nueva deuda" abre modal de creación
- Click en tarjeta navega al detalle

### 2. Página detalle `/debts/:id`
- Info completa de la deuda arriba
- Botones: "Registrar pago" (modal), "Editar" (modal), "Eliminar" (confirmación)
- Tabla de amortización con columnas: #, Fecha vencimiento, Capital, Interés,
  Cuota, Saldo, Estado (badge: pending=amarillo, paid=verde, overdue=rojo)
- Las filas con status='paid' deben verse atenuadas

### 3. Modal "Nueva deuda"
Campos del formulario:
- creditor (texto, requerido)
- description (texto, requerido)
- debtTypeId (select con los 6 tipos)
- frequencyId (select: mensual / quincenal)
- originalAmount (número, requerido)
- interestRate (número, requerido — ejemplo: 2.5 para 2.5%)
- interestRateType (radio: mensual / anual)
- totalInstallments (número entero, requerido)
- startDate (date picker, requerido)
- notes (textarea, opcional)

Al crear exitosamente, mostrar la cuota calculada antes de confirmar.
Fórmula cuota francesa (preview en tiempo real):
  r = interestRate / 100  (si es mensual)
  r = (1 + interestRate/100)^(1/12) - 1  (si es anual)
  cuota = P * r * (1+r)^n / ((1+r)^n - 1)

### 4. Modal "Registrar pago"
- paymentType: radio "Regular" / "Extraordinario"
- paymentDate: date picker (default hoy)
- Si es regular: mostrar la próxima cuota pendiente del cronograma (readonly, datos del schedule con status='pending' más bajo installmentNumber)
- Si es extra:
  - totalAmount: campo editable (obligatorio)
  - extraPaymentStrategy: select "Reducir cuota" (reduce_installment) / "Reducir plazo" (reduce_term)
- notes (opcional)

---

## COMPORTAMIENTO CLAVE

- Para pagos **regular**: el backend ignora el `totalAmount` enviado y usa el
  monto de la próxima cuota pendiente del cronograma automáticamente.
  Solo importa `paymentDate` y `notes`. Igualmente enviar `totalAmount` con
  el valor de la cuota para pasar la validación `@NotNull`.
- Para pagos **extra**: el `totalAmount` va íntegro a capital. Si se elige
  `reduce_installment` la cuota baja pero el plazo se mantiene; con `reduce_term`
  el plazo se acorta pero la cuota no cambia.
- Después de cada pago, recargar el detalle de la deuda y el cronograma para
  reflejar el nuevo estado.
- Si `status === 'paid_off'`, ocultar botón "Registrar pago" y mostrar badge "Saldada".
- `progressPercentage` viene calculado del backend, usarlo directamente.

---

## SERVICIO Angular

```typescript
@Injectable({ providedIn: 'root' })
export class DebtService {
  private http = inject(HttpClient);
  private base = 'http://localhost:9000/api/debts';

  getAll(status?: string): Observable<Debt[]>
  getDetail(id: string): Observable<DebtDetail>       // retorna { debt, schedule }
  getSummary(): Observable<DebtSummary>
  create(req: CreateDebtRequest): Observable<Debt>
  update(id: string, req: { creditor: string; description: string; notes?: string }): Observable<Debt>
  delete(id: string): Observable<void>
  registerPayment(id: string, req: RegisterPaymentRequest): Observable<DebtPayment>
  getSchedule(id: string): Observable<DebtScheduleItem[]>
}
```

---

## MANEJO DE ERRORES

| HTTP Status | Situación | Mensaje al usuario |
|-------------|-----------|-------------------|
| 404 | Deuda no encontrada | "Deuda no encontrada" |
| 400 | Validación fallida | Mostrar mensaje del backend |
| 500 | Error de servidor | "Error inesperado, intenta de nuevo" |
| 422 / IllegalState | No hay cuotas pendientes | "Esta deuda no tiene cuotas pendientes" |
