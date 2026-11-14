package com.bibliotech.web;

import java.time.LocalDate;

public record EmpruntLigne(
        long id,
        String titreLivre,
        long etudiantId,
        LocalDate dateEmprunt,
        LocalDate dateRetourPrevue,
        String statut,
        double penalite
) {
}
