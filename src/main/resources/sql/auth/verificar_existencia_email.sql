-- Verificar si existe un usuario con el email dado
SELECT COUNT(1) > 0 AS exists_flag FROM users WHERE email = :email
