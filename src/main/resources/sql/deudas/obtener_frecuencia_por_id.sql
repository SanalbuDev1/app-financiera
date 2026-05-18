-- Obtiene una frecuencia de pago por su ID.
-- Parámetro: :id
SELECT
    id,
    name,
    days_between_payments
FROM payment_frequencies
WHERE id = :id
