-- Obtener una transaccion por su ID con categoria y tipo resueltos
SELECT t.id, t.user_id, t.description, t.amount,
       c.name AS category_name,
       tt.name AS type_name,
       t.transaction_date, t.notes, t.created_at
FROM transactions t
INNER JOIN categories c ON t.category_id = c.id
INNER JOIN transaction_types tt ON t.type_id = tt.id
WHERE t.id = :id
