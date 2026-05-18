-- Lista todas las frecuencias de pago disponibles.
SELECT
    id,
    name,
    days_between_payments
FROM payment_frequencies
ORDER BY days_between_payments DESC
