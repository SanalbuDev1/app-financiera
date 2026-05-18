-- Obtiene un tipo de deuda por su ID.
-- Parámetro: :id
SELECT
    id,
    name,
    description,
    icon,
    active
FROM debt_types
WHERE id = :id
