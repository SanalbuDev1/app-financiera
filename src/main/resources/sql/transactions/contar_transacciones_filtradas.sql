-- Cuenta el total de transacciones que coinciden con los filtros dinámicos.
-- Usado para calcular totalElements y totalPages en la paginación.
SELECT COUNT(*) AS total
FROM transactions t
INNER JOIN categories c ON t.category_id = c.id
INNER JOIN transaction_types tt ON t.type_id = tt.id
WHERE t.user_id = :userId
  AND (:fromDate IS NULL OR t.transaction_date >= :fromDate)
  AND (:toDate IS NULL OR t.transaction_date <= :toDate)
  AND (:typeName IS NULL OR tt.name = :typeName)
  AND (:categoryName IS NULL OR c.name = :categoryName)
