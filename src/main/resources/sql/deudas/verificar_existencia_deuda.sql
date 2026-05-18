-- Verifica si existe una deuda con el ID dado.
-- Parámetro: :id
SELECT EXISTS (
    SELECT 1 FROM debts WHERE id = :id
) AS exists
