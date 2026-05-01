-- Registrar un nuevo usuario en el sistema
INSERT INTO users (id, email, name, password, role)
VALUES (:id, :email, :name, :password, :role)
