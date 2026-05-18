# Sistema de Amortización Francesa

## ¿Qué es?

El **sistema de amortización francés** (también llamado "de cuota fija") es el método más común usado por bancos y entidades financieras en Colombia y Latinoamérica.

**Característica principal:** La cuota mensual es **siempre la misma** durante toda la vida del préstamo.

## ¿Cómo funciona?

Cada cuota tiene dos componentes:
- **Intereses** - Se calculan sobre el saldo pendiente
- **Capital** - Lo que realmente abona a la deuda

```
┌─────────────────────────────────────────────────────────────┐
│                    CUOTA FIJA = $916,800                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Primera cuota:  █████████████████████░░░░░░░░░░░░░░░░░░░  │
│                  ├── Interés: $150,000 ──┤├─ Capital: $766,800 ─┤
│                                                             │
│  Última cuota:   ░░░░░░░░░░░░░░░░░░░░░█████████████████████  │
│                  ├─ Interés: $13,500 ─┤├── Capital: $903,300 ──┤
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Al inicio:** Pagas más intereses y menos capital
**Al final:** Pagas menos intereses y más capital

## Fórmula Matemática

```
         P × r × (1 + r)ⁿ
Cuota = ─────────────────
          (1 + r)ⁿ - 1
```

Donde:
- **P** = Principal (monto del préstamo)
- **r** = Tasa de interés por período (mensual)
- **n** = Número de cuotas

### Ejemplo de cálculo paso a paso

**Datos del préstamo:**
- Monto: $10,000,000
- Tasa: 1.5% mensual
- Plazo: 12 meses

**Paso 1: Convertir tasa a decimal**
```
r = 1.5% = 0.015
```

**Paso 2: Calcular (1 + r)ⁿ**
```
(1 + 0.015)¹² = 1.015¹² = 1.1956
```

**Paso 3: Aplicar la fórmula**
```
         10,000,000 × 0.015 × 1.1956
Cuota = ─────────────────────────────
              1.1956 - 1

         179,342.73
Cuota = ───────────
          0.1956

Cuota = $916,799.93
```

## Cálculo de cada cuota

Para cada período:

```
Interés del período = Saldo pendiente × Tasa mensual
Capital del período = Cuota fija - Interés
Nuevo saldo = Saldo anterior - Capital
```

### Primera cuota (mes 1)
```
Saldo inicial:     $10,000,000.00
Interés:           $10,000,000 × 0.015 = $150,000.00
Capital:           $916,799.93 - $150,000 = $766,799.93
Nuevo saldo:       $10,000,000 - $766,799.93 = $9,233,200.07
```

### Segunda cuota (mes 2)
```
Saldo inicial:     $9,233,200.07
Interés:           $9,233,200.07 × 0.015 = $138,498.00
Capital:           $916,799.93 - $138,498 = $778,301.93
Nuevo saldo:       $9,233,200.07 - $778,301.93 = $8,454,898.14
```

## Conversión de tasas

### De tasa anual a mensual (tasa efectiva)

```
r_mensual = (1 + r_anual)^(1/12) - 1
```

**Ejemplo:** Tasa anual del 18%
```
r_mensual = (1 + 0.18)^(1/12) - 1
r_mensual = 1.18^0.0833 - 1
r_mensual = 1.0139 - 1
r_mensual = 0.0139 = 1.39% mensual
```

### De tasa mensual a quincenal

```
r_quincenal = (1 + r_mensual)^(1/2) - 1
```

**Ejemplo:** Tasa mensual del 1.5%
```
r_quincenal = (1 + 0.015)^0.5 - 1
r_quincenal = 1.015^0.5 - 1
r_quincenal = 1.00747 - 1
r_quincenal = 0.00747 = 0.747% quincenal
```

## Ventajas del sistema francés

1. **Predictibilidad** - Siempre sabes cuánto vas a pagar
2. **Facilidad de planificación** - El gasto es constante mes a mes
3. **Ampliamente usado** - Los bancos lo usan, fácil de comparar

## Desventajas

1. **Al inicio pagas más intereses** - En los primeros meses, la mayor parte de tu cuota son intereses
2. **Si cancelas anticipado, ya pagaste muchos intereses** - No es tan beneficioso cancelar al final del crédito

## Comparación: Sistema Francés vs Alemán

| Característica | Francés (Cuota fija) | Alemán (Amortización fija) |
|----------------|---------------------|---------------------------|
| Cuota | Siempre igual | Decrece cada mes |
| Capital | Aumenta cada mes | Siempre igual |
| Intereses totales | Ligeramente más | Ligeramente menos |
| Uso común | Bancos, vehículos | Menos común |

## Implementación en el sistema

El cálculo se realiza en `CalculateAmortizationUseCase.java`:

```java
// Fórmula de cuota fija
BigDecimal onePlusRate = BigDecimal.ONE.add(periodRate);
BigDecimal onePlusRatePowN = onePlusRate.pow(installments);
BigDecimal numerator = principal.multiply(periodRate).multiply(onePlusRatePowN);
BigDecimal denominator = onePlusRatePowN.subtract(BigDecimal.ONE);
BigDecimal installment = numerator.divide(denominator);
```

Ver código completo en: `src/main/java/com/finanzas/personales/finanzas/deudas/domain/usecase/CalculateAmortizationUseCase.java`
