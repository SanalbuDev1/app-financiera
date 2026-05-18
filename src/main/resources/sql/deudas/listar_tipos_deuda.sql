-- Lista todos los tipos de deuda activos, ordenados por nombre.
SELECT
    id,
    name,
    description,
    icon,
    active
FROM debt_types
WHERE active = TRUE
ORDER BY name ASC
