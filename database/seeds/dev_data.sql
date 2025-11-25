-- Données de développement
-- Ne pas exécuter en production !

-- Exemple d'utilisateur de test
-- Password: "password123" (hashé avec BCrypt)
INSERT INTO users (email, password_hash, first_name, last_name) VALUES
('test@example.com', '$2a$10$...hash...', 'Test', 'User');
