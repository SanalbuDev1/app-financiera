-- Elimina una deuda por ID. Los registros en debt_payments y debt_schedule
-- se eliminan automáticamente gracias al FK ON DELETE CASCADE.
-- Parámetro: :id
DELETE FROM debts
WHERE id = :id
