package com.bibliotech.model;

import java.time.LocalDate;

public record Emprunt(
        long id,
        long livreId,
        long etudiantId,
        LocalDate dateEmprunt,
        LocalDate dateRetourPrevue,
        LocalDate dateRetourEffective,
        StatutEmprunt statut
) {
}