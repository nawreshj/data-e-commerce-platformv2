-- Données initiales pour la base de données H2
-- Ce script est exécuté automatiquement au démarrage de l'application
-- Création de la table users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
INSERT INTO users (id, first_name, last_name, email, password_hash, active, created_at, updated_at)
VALUES
(1, 'Alice', 'Dupont', 'alice.dupont@example.com', '$2b$10$FLOk5RqHf.Cbf2CdP/jX2O1MZD5nYE6.HXmNz7fnZMZrkIQIj.rBG', TRUE, CURRENT_TIMESTAMP, NULL),
(2, 'Bob', 'Martin', 'bob.martin@example.com',   '$2b$10$FLOk5RqHf.Cbf2CdP/jX2O1MZD5nYE6.HXmNz7fnZMZrkIQIj.rBG', TRUE, CURRENT_TIMESTAMP, NULL),
(3, 'Charlie', 'Durand', 'charlie.durand@example.com', '$2b$10$FLOk5RqHf.Cbf2CdP/jX2O1MZD5nYE6.HXmNz7fnZMZrkIQIj.rBG', TRUE, CURRENT_TIMESTAMP, NULL);