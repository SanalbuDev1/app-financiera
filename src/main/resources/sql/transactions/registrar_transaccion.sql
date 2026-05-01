-- Registrar una nueva transaccion
INSERT INTO transactions (id, user_id, description, amount, category_id, type_id, transaction_date, notes, created_at)
VALUES (:id, :userId, :description, :amount, :categoryId, :typeId, :transactionDate, :notes, :createdAt)
