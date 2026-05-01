-- Verificar si existe una transaccion por su ID
SELECT COUNT(1) > 0 AS exists_flag FROM transactions WHERE id = :id
