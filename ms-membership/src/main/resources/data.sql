-- Données initiales pour la base de données H2
-- Ce script est exécuté automatiquement au démarrage de l'application
-- Création de la table users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Insertion des utilisateurs initiaux
INSERT INTO users (first_name, last_name, email, active, created_at, updated_at)
VALUES
('Alice', 'Dupont', 'alice.dupont@example.com', TRUE, CURRENT_TIMESTAMP, NULL),
('Bob', 'Martin', 'bob.martin@example.com', TRUE, CURRENT_TIMESTAMP, NULL),
('Charlie', 'Durand', 'charlie.durand@example.com', TRUE, CURRENT_TIMESTAMP, NULL);