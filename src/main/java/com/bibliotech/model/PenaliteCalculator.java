package com.bibliotech.model;

public class PenaliteCalculator {

    private static final double TARIF_PAR_JOUR = 0.50;
    private static final double PLAFOND = 30.0;

    private PenaliteCalculator() {} // classe utilitaire : pas d'instanciation

    public static double calculerPenalite(StatutEmprunt statut) {
        return switch (statut) {
            case StatutEmprunt.EnCours e -> 0.0;
            case StatutEmprunt.Rendu r when !r.enRetardLorsDuRetour() -> 0.0;
            case StatutEmprunt.Rendu r -> TARIF_PAR_JOUR; // voir remarque ci-dessous
            case StatutEmprunt.EnRetard r ->
                    Math.min(r.joursDeRetard() * TARIF_PAR_JOUR, PLAFOND);
        };
    }
}