package com.bibliotech.model;

import java.util.regex.Pattern;

public record Etudiant(
        long id,
        String numeroEtudiant,
        String nom,
        String prenom,
        String email
) {
    private static final Pattern NUMERO_PATTERN = Pattern.compile("[A-Z]{2}\\d{6}");

    public Etudiant {
        if (numeroEtudiant == null || numeroEtudiant.isBlank()) {
            throw new IllegalArgumentException("Le numéro étudiant ne peut pas être vide");
        }
        if (!NUMERO_PATTERN.matcher(numeroEtudiant).matches()) {
            throw new IllegalArgumentException(
                    "Le numéro étudiant doit respecter le format XX000000 (ex: ET123456)");
        }
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        }
        if (prenom == null || prenom.isBlank()) {
            throw new IllegalArgumentException("Le prénom ne peut pas être vide");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("L'email doit être valide et contenir un @");
        }
    }
}