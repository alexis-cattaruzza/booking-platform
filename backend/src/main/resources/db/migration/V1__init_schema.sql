-- V1: Schema initial
-- Date: 2024-11-25
-- Description: Tables principales pour le système de réservation

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'business' CHECK (role IN ('business', 'customer', 'admin')),
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index sur email pour les recherches rapides
CREATE INDEX idx_users_email ON users(email);

-- TODO: Ajouter les autres tables selon l'architecture
-- (businesses, services, schedules, appointments, customers, etc.)
