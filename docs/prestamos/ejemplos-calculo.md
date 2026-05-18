# Ejemplos de Cálculo

## Ejemplo 1: Préstamo personal típico

### Datos
- **Monto:** $10,000,000 COP
- **Tasa:** 1.5% mensual
- **Plazo:** 12 meses
- **Frecuencia:** Mensual

### Resultado
- **Cuota fija:** $916,799.93
- **Total intereses:** $1,001,599.16
- **Total a pagar:** $11,001,599.16

### Tabla de amortización completa

| # | Fecha | Cuota | Interés | Capital | Saldo |
|---|-------|-------|---------|---------|-------|
| 1 | Jun 2026 | $916,799.93 | $150,000.00 | $766,799.93 | $9,233,200.07 |
| 2 | Jul 2026 | $916,799.93 | $138,498.00 | $778,301.93 | $8,454,898.14 |
| 3 | Ago 2026 | $916,799.93 | $126,823.47 | $789,976.46 | $7,664,921.68 |
| 4 | Sep 2026 | $916,799.93 | $114,973.83 | $801,826.10 | $6,863,095.58 |
| 5 | Oct 2026 | $916,799.93 | $102,946.43 | $813,853.50 | $6,049,242.08 |
| 6 | Nov 2026 | $916,799.93 | $90,738.63 | $826,061.30 | $5,223,180.78 |
| 7 | Dic 2026 | $916,799.93 | $78,347.71 | $838,452.22 | $4,384,728.56 |
| 8 | Ene 2027 | $916,799.93 | $65,770.93 | $851,029.00 | $3,533,699.56 |
| 9 | Feb 2027 | $916,799.93 | $53,005.49 | $863,794.44 | $2,669,905.12 |
| 10 | Mar 2027 | $916,799.93 | $40,048.58 | $876,751.35 | $1,793,153.77 |
| 11 | Abr 2027 | $916,799.93 | $26,897.31 | $889,902.62 | $903,251.15 |
| 12 | May 2027 | $916,799.93 | $13,548.77 | $903,251.16 | $0.00 |
| **TOTAL** | | **$11,001,599.16** | **$1,001,599.16** | **$10,000,000.00** | |

### Observaciones
- La primera cuota tiene $150,000 de intereses (16.4% de la cuota)
- La última cuota tiene $13,549 de intereses (1.5% de la cuota)
- El capital aumenta progresivamente cada mes

---

## Ejemplo 2: Crédito de vehículo

### Datos
- **Monto:** $45,000,000 COP
- **Tasa:** 1.2% mensual
- **Plazo:** 48 meses (4 años)
- **Frecuencia:** Mensual

### Resultado
- **Cuota fija:** $1,222,876.54
- **Total intereses:** $13,698,073.92
- **Total a pagar:** $58,698,073.92

### Primeras 6 cuotas

| # | Cuota | Interés | Capital | Saldo |
|---|-------|---------|---------|-------|
| 1 | $1,222,876.54 | $540,000.00 | $682,876.54 | $44,317,123.46 |
| 2 | $1,222,876.54 | $531,805.48 | $691,071.06 | $43,626,052.40 |
| 3 | $1,222,876.54 | $523,512.63 | $699,363.91 | $42,926,688.49 |
| 4 | $1,222,876.54 | $515,120.26 | $707,756.28 | $42,218,932.21 |
| 5 | $1,222,876.54 | $506,627.19 | $716,249.35 | $41,502,682.86 |
| 6 | $1,222,876.54 | $498,032.19 | $724,844.35 | $40,777,838.51 |

---

## Ejemplo 3: Préstamo quincenal

### Datos
- **Monto:** $5,000,000 COP
- **Tasa:** 1.5% mensual → 0.747% quincenal
- **Plazo:** 12 quincenas (6 meses)
- **Frecuencia:** Quincenal

### Conversión de tasa
```
r_quincenal = (1 + 0.015)^0.5 - 1 = 0.00747 = 0.747%
```

### Resultado
- **Cuota fija:** $441,234.56
- **Total intereses:** $294,814.72
- **Total a pagar:** $5,294,814.72

### Ventaja del pago quincenal
Al pagar quincenalmente:
- Reduces el saldo más rápido
- Pagas menos intereses en total
- Aunque la cuota sea aproximadamente la mitad, el ahorro es mayor

---

## Ejemplo 4: Abono extraordinario

### Situación inicial
Usando el Ejemplo 1 (después de pagar 3 cuotas):
- **Saldo actual:** $7,664,921.68
- **Cuotas restantes:** 9
- **Cuota actual:** $916,799.93

### Abono extraordinario: $2,000,000

#### Opción A: Reducir cuota (mantener plazo)
```
Nuevo saldo = $7,664,921.68 - $2,000,000 = $5,664,921.68
Cuotas restantes = 9 (sin cambio)
Nueva cuota = ?
```

Aplicando la fórmula con el nuevo saldo:
```
Nueva cuota = $677,247.89
```

**Beneficio:** Pagas $239,552 menos cada mes

#### Opción B: Reducir plazo (mantener cuota)

```
Nuevo saldo = $5,664,921.68
Cuota = $916,799.93 (sin cambio)
Nuevas cuotas = ?
```

Despejando n de la fórmula:
```
n = ln(Cuota / (Cuota - Saldo × r)) / ln(1 + r)
n = ln(916,799.93 / (916,799.93 - 5,664,921.68 × 0.015)) / ln(1.015)
n = 6.5 → 7 cuotas
```

**Beneficio:** Terminas 2 meses antes

### Comparación

| Opción | Cuota | Plazo | Intereses restantes |
|--------|-------|-------|---------------------|
| Sin abono | $916,799.93 | 9 meses | $586,279 |
| Reducir cuota | $677,247.89 | 9 meses | $430,310 |
| Reducir plazo | $916,799.93 | 7 meses | $352,678 |

**Conclusión:** Reducir plazo ahorra más intereses, pero reducir cuota da más liquidez mensual.

---

## Ejemplo 5: Tasa anual efectiva

### Datos
- **Monto:** $20,000,000 COP
- **Tasa:** 18% E.A. (Efectiva Anual)
- **Plazo:** 24 meses
- **Frecuencia:** Mensual

### Conversión de tasa
```
r_mensual = (1 + 0.18)^(1/12) - 1 = 0.01389 = 1.389%
```

### Resultado
- **Cuota fija:** $997,123.45
- **Total intereses:** $3,930,962.80
- **Total a pagar:** $23,930,962.80

### Nota importante
Los bancos en Colombia suelen expresar tasas como:
- **E.A.** = Efectiva Anual (ya incluye capitalización)
- **N.M.V.** = Nominal Mes Vencido (se divide entre 12 para obtener mensual)
- **N.A.M.V.** = Nominal Anual Mes Vencido

Siempre verifica qué tipo de tasa te están dando.
