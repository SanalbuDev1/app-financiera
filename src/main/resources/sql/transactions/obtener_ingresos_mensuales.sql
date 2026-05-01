-- Calcula la suma de ingresos del usuario para un mes y año específico.
SELECT COALESCE(SUM(t.amount), 0) AS monthly_income
FROM transactions t
INNER JOIN transaction_types tt ON t.type_id = tt.id
WHERE t.user_id = :userId
  AND tt.name = 'income'
  AND EXTRACT(MONTH FROM t.transaction_date) = :month
  AND EXTRACT(YEAR FROM t.transaction_date) = :year
