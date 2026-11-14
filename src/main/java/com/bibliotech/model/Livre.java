package com.bibliotech.model;

import java.time.Year;

public record Livre(
        long id,
        String titre,
        String auteur,
        int anneePublication,
        int exemplairesTotal,
        int exemplairesDisponibles
) {
    public Livre {
        if (titre == null || titre.isBlank()) {
            throw new ChampInvalideException("titre", "Le titre ne peut pas être vide");
        }
        if (auteur == null || auteur.isBlank()) {
            throw new ChampInvalideException("auteur", "L'auteur ne peut pas être vide");
        }
        int anneeCourante = Year.now().getValue();
        if (anneePublication < 1500 || anneePublication > anneeCourante) {
            throw new ChampInvalideException("anneePublication",
                    "L'année de publication doit être entre 1500 et " + anneeCourante);
        }
        if (exemplairesDisponibles > exemplairesTotal) {
            throw new ChampInvalideException("exemplairesDisponibles",
                    "Les exemplaires disponibles ne peuvent pas dépasser le total");
        }
    }
}