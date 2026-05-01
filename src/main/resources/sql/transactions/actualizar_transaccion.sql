-- Actualizar una transaccion existente que pertenece al usuario
UPDATE transactions
SET description = :description,
    amount = :amount,
    category_id = :categoryId,
    type_id = :typeId,
    transaction_date = :transactionDate,
    notes = :notes
WHERE id = :id
  AND user_id = :userId
