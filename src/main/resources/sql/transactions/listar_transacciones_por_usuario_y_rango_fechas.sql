-- Listar transacciones de un usuario en un rango de fechas
SELECT t.id, t.user_id, t.description, t.amount,
       c.name AS category_name,
       tt.name AS type_name,
       t.transaction_date, t.notes, t.created_at
FROM transactions t
INNER JOIN categories c ON t.category_id = c.id
INNER JOIN transaction_types tt ON t.type_id = tt.id
WHERE t.user_id = :userId
  AND t.transaction_date BETWEEN :startDate AND :endDate
ORDER BY t.transaction_date DESC
