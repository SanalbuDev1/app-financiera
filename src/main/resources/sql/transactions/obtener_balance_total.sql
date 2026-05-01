-- Calcula el balance total histórico del usuario.
-- Balance = suma de ingresos - suma de gastos.
SELECT COALESCE(
    SUM(CASE WHEN tt.name = 'income' THEN t.amount ELSE 0 END) -
    SUM(CASE WHEN tt.name = 'expense' THEN t.amount ELSE 0 END),
    0
) AS total_balance
FROM transactions t
INNER JOIN transaction_types tt ON t.type_id = tt.id
WHERE t.user_id = :userId
