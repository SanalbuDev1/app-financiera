-- Modificar una transaccion existente por su ID
UPDATE transactions
SET description = :description,
    amount = :amount,
    category_id = :categoryId,
    type_id = :typeId,
    transaction_date = :transactionDate,
    notes = :notes
WHERE id = :id
