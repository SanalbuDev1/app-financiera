-- Elimina una transacción solo si pertenece al usuario indicado.
-- Retorna filas afectadas: 1 si se eliminó, 0 si no existía o no era del usuario.
DELETE FROM transactions
WHERE id = :id AND user_id = :userId
