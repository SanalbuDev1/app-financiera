-- Obtiene transacciones paginadas con filtros dinámicos opcionales.
-- Los filtros se aplican con CASE WHEN para manejar parámetros nulos.
SELECT t.id, t.user_id, t.description, t.amount,
       c.name AS category_name, tt.name AS type_name,
       t.transaction_date, t.notes, t.created_at
FROM transactions t
INNER JOIN categories c ON t.category_id = c.id
INNER JOIN transaction_types tt ON t.type_id = tt.id
WHERE t.user_id = :userId
  AND (:fromDate IS NULL OR t.transaction_date >= :fromDate)
  AND (:toDate IS NULL OR t.transaction_date <= :toDate)
  AND (:typeName IS NULL OR tt.name = :typeName)
  AND (:categoryName IS NULL OR c.name = :categoryName)
ORDER BY t.transaction_date DESC
LIMIT :limit OFFSET :offset
