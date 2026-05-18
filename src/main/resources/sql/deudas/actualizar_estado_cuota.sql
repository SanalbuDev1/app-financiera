-- Actualiza el estado de un ítem del cronograma.
-- Estados válidos: 'pending', 'paid', 'partial', 'overdue'
-- Parámetros: :status, :id
UPDATE debt_schedule
SET status = :status
WHERE id = :id
