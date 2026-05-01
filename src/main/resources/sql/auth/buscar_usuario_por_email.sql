-- Buscar un usuario por su correo electronico
SELECT id, email, name, password, role
FROM users
WHERE email = :email
