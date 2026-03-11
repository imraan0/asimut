-- ============================================================
-- SCHEMA SQL — Asimut
-- Base de données : PostgreSQL 16
-- ============================================================

-- Suppression des tables si elles existent déjà 
DROP TABLE IF EXISTS eleve_option CASCADE;
DROP TABLE IF EXISTS participation CASCADE;
DROP TABLE IF EXISTS attestation CASCADE;
DROP TABLE IF EXISTS convention CASCADE;
DROP TABLE IF EXISTS recherche_stage CASCADE;
DROP TABLE IF EXISTS moyenne CASCADE;
DROP TABLE IF EXISTS semestre CASCADE;
DROP TABLE IF EXISTS parent CASCADE;
DROP TABLE IF EXISTS eleve CASCADE;
DROP TABLE IF EXISTS classe CASCADE;
DROP TABLE IF EXISTS niveau CASCADE;
DROP TABLE IF EXISTS projet CASCADE;
DROP TABLE IF EXISTS option CASCADE;
DROP TABLE IF EXISTS professeur CASCADE;
DROP TABLE IF EXISTS secretariat CASCADE;
DROP TABLE IF EXISTS proviseur CASCADE;
DROP TABLE IF EXISTS utilisateur CASCADE;

-- Types ENUM
DROP TYPE IF EXISTS role_utilisateur CASCADE;
DROP TYPE IF EXISTS type_option CASCADE;
DROP TYPE IF EXISTS statut_stage CASCADE;

CREATE TYPE role_utilisateur AS ENUM ('eleve', 'professeur', 'secretariat', 'proviseur');
CREATE TYPE type_option      AS ENUM ('langue', 'technique');
CREATE TYPE statut_stage AS ENUM ('non_contacte', 'en_attente', 'refuse', 'entretien_accorde', 'entretien_refuse', 'valide');

-- ============================================================
-- AUTHENTIFICATION
-- ============================================================

CREATE TABLE utilisateur (
    id         SERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,         -- toujours hashé avec bcrypt
    role       role_utilisateur NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- PERSONNEL
-- ============================================================

CREATE TABLE professeur (
    id             SERIAL PRIMARY KEY,
    utilisateur_id INT NOT NULL UNIQUE REFERENCES utilisateur(id) ON DELETE CASCADE,
    nom            VARCHAR(100) NOT NULL,
    prenom         VARCHAR(100) NOT NULL
);

CREATE TABLE secretariat (
    id             SERIAL PRIMARY KEY,
    utilisateur_id INT NOT NULL UNIQUE REFERENCES utilisateur(id) ON DELETE CASCADE,
    nom            VARCHAR(100) NOT NULL,
    prenom         VARCHAR(100) NOT NULL
);

CREATE TABLE proviseur (
    id             SERIAL PRIMARY KEY,
    utilisateur_id INT NOT NULL UNIQUE REFERENCES utilisateur(id) ON DELETE CASCADE,
    nom            VARCHAR(100) NOT NULL,
    prenom         VARCHAR(100) NOT NULL
);

-- ============================================================
-- SCOLARITÉ
-- ============================================================

CREATE TABLE niveau (
    id     SERIAL PRIMARY KEY,
    numero INT NOT NULL UNIQUE   -- 6, 5, 4, 3
);

CREATE TABLE classe (
    id             SERIAL PRIMARY KEY,
    niveau_id      INT NOT NULL REFERENCES niveau(id) ON DELETE RESTRICT,
    lettre         VARCHAR(2) NOT NULL,       -- A, B, C...
    annee_scolaire VARCHAR(9) NOT NULL        -- ex: 2025-2026
);

CREATE TABLE eleve (
    id             SERIAL PRIMARY KEY,
    utilisateur_id INT NOT NULL UNIQUE REFERENCES utilisateur(id) ON DELETE CASCADE,
    classe_id      INT NOT NULL REFERENCES classe(id) ON DELETE RESTRICT,
    professeur_id  INT REFERENCES professeur(id) ON DELETE SET NULL,  -- référent
    nom            VARCHAR(100) NOT NULL,
    prenom         VARCHAR(100) NOT NULL,
    identifiant    VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE parent (
    id       SERIAL PRIMARY KEY,
    eleve_id INT NOT NULL UNIQUE REFERENCES eleve(id) ON DELETE CASCADE,  -- 1 seul parent
    nom      VARCHAR(100) NOT NULL,
    prenom   VARCHAR(100) NOT NULL,
    email    VARCHAR(255) NOT NULL
);

-- ============================================================
-- MOYENNES
-- ============================================================

CREATE TABLE semestre (
    id             SERIAL PRIMARY KEY,
    numero         INT NOT NULL,          
    annee_scolaire VARCHAR(9) NOT NULL,   
    UNIQUE(numero, annee_scolaire)
);

CREATE TABLE moyenne (
    id          SERIAL PRIMARY KEY,
    eleve_id    INT NOT NULL REFERENCES eleve(id) ON DELETE CASCADE,
    semestre_id INT NOT NULL REFERENCES semestre(id) ON DELETE RESTRICT,
    valeur      DECIMAL(4,2) NOT NULL,      
    valide      BOOLEAN DEFAULT FALSE,        -- verrou proviseur
    created_at  TIMESTAMP DEFAULT NOW(),
    UNIQUE(eleve_id, semestre_id)             -- 1 moyenne par élève par semestre
);

-- ============================================================
-- STAGES
-- ============================================================

CREATE TABLE recherche_stage (
    id                   SERIAL PRIMARY KEY,
    eleve_id             INT NOT NULL REFERENCES eleve(id) ON DELETE CASCADE,
    nom_entreprise       VARCHAR(255) NOT NULL,
    nom_contact          VARCHAR(255),
    email_contact        VARCHAR(255),
    nb_lettres_envoyees  INT DEFAULT 0,
    nb_lettres_recues    INT DEFAULT 0,
    date_entretien       DATE,
    resultat             TEXT,
    statut               statut_stage DEFAULT 'non_contacte',
    created_at           TIMESTAMP DEFAULT NOW()
);

CREATE TABLE convention (
    id          SERIAL PRIMARY KEY,
    eleve_id    INT NOT NULL REFERENCES eleve(id) ON DELETE CASCADE,
    date_debut  DATE NOT NULL,
    date_fin    DATE NOT NULL,
    valide      BOOLEAN DEFAULT FALSE,
    pdf_path    VARCHAR(500),
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE attestation (
    id              SERIAL PRIMARY KEY,
    convention_id   INT NOT NULL UNIQUE REFERENCES convention(id) ON DELETE CASCADE,
    pdf_path        VARCHAR(500),
    date_signature  DATE
);

-- ============================================================
-- PROJETS
-- ============================================================

CREATE TABLE projet (
    id          SERIAL PRIMARY KEY,
    nom         VARCHAR(255) NOT NULL,
    objectif    TEXT NOT NULL,
    date_debut  DATE NOT NULL,
    date_fin    DATE NOT NULL,
    valide      BOOLEAN DEFAULT FALSE,   -- validé par le secrétariat
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE participation (
    id               SERIAL PRIMARY KEY,
    eleve_id         INT NOT NULL REFERENCES eleve(id) ON DELETE CASCADE,
    projet_id        INT NOT NULL REFERENCES projet(id) ON DELETE CASCADE,
    est_responsable  BOOLEAN DEFAULT FALSE,
    date_debut       DATE,
    date_fin         DATE,
    UNIQUE(eleve_id, projet_id)          -- un élève ne participe qu'une fois par projet
);

-- ============================================================
-- OPTIONS
-- ============================================================

CREATE TABLE option (
    id   SERIAL PRIMARY KEY,
    nom  VARCHAR(100) NOT NULL,
    type type_option NOT NULL
);

-- Table de liaison Eleve - Option (max 2 options par élève, géré dans l'API)
CREATE TABLE eleve_option (
    eleve_id  INT NOT NULL REFERENCES eleve(id) ON DELETE CASCADE,
    option_id INT NOT NULL REFERENCES option(id) ON DELETE CASCADE,
    PRIMARY KEY (eleve_id, option_id)
);

-- ============================================================
-- DONNÉES DE BASE (niveaux)
-- ============================================================
INSERT INTO niveau (numero) VALUES (6), (5), (4), (3);