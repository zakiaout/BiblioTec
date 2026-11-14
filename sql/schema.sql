-- BiblioTech : schéma + données de démo
DROP TABLE IF EXISTS emprunt CASCADE;
DROP TABLE IF EXISTS etudiant CASCADE;
DROP TABLE IF EXISTS livre CASCADE;

CREATE TABLE livre (
    id                      BIGSERIAL PRIMARY KEY,
    titre                   VARCHAR(255) NOT NULL,
    auteur                  VARCHAR(255) NOT NULL,
    annee_publication       INT          NOT NULL,
    exemplaires_total       INT          NOT NULL,
    exemplaires_disponibles INT          NOT NULL
);

CREATE TABLE etudiant (
    id              BIGSERIAL PRIMARY KEY,
    numero_etudiant VARCHAR(8)   NOT NULL UNIQUE,
    nom             VARCHAR(100) NOT NULL,
    prenom          VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL
);

CREATE TABLE emprunt (
    id                    BIGSERIAL PRIMARY KEY,
    livre_id              BIGINT      NOT NULL REFERENCES livre(id),
    etudiant_id           BIGINT      NOT NULL REFERENCES etudiant(id),
    date_emprunt          DATE        NOT NULL,
    date_retour_prevue    DATE        NOT NULL,
    date_retour_effective DATE,
    statut                VARCHAR(20) NOT NULL
);

INSERT INTO livre (titre, auteur, annee_publication, exemplaires_total, exemplaires_disponibles) VALUES
    ('Effective Java',  'Joshua Bloch',     2018, 1, 1),
    ('Clean Code',      'Robert C. Martin', 2008, 2, 2),
    ('Design Patterns', 'Erich Gamma',      1994, 3, 3),
    ('Refactoring',     'Martin Fowler',    2018, 2, 2),
    ('Head First Java', 'Kathy Sierra',     2005, 4, 4);

INSERT INTO etudiant (numero_etudiant, nom, prenom, email) VALUES
    ('ET123456', 'Dupont', 'Alice', 'alice.dupont@univ.fr'),
    ('ET654321', 'Martin', 'Bob',   'bob.martin@univ.fr');