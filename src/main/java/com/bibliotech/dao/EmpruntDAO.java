package com.bibliotech.dao;

import com.bibliotech.model.Emprunt;
import com.bibliotech.model.StatutEmprunt;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmpruntDAO implements GenericDAO<Emprunt, Long> {

    private static final String COLONNES =
            "id, livre_id, etudiant_id, date_emprunt, date_retour_prevue, "
                    + "date_retour_effective, statut";

    @Override
    public List<Emprunt> findAll() {
        return lister("SELECT " + COLONNES + " FROM emprunt ORDER BY id");
    }

    /** Emprunts dont la date prévue est passée et qui ne sont pas rendus. */
    public List<Emprunt> findEnRetard() {
        return lister("SELECT " + COLONNES + " FROM emprunt "
                + "WHERE date_retour_prevue < CURRENT_DATE "
                + "AND date_retour_effective IS NULL ORDER BY date_retour_prevue");
    }

    /** Emprunts non rendus (donc en cours OU en retard). */
    public List<Emprunt> findEnCours() {
        return lister("SELECT " + COLONNES + " FROM emprunt "
                + "WHERE date_retour_effective IS NULL ORDER BY date_retour_prevue");
    }

    /** Enregistre le retour d'un livre : marque l'emprunt rendu ET remet 1 exemplaire en stock. */
    public void enregistrerRetour(long empruntId) {
        String retourSql = "UPDATE emprunt SET date_retour_effective = CURRENT_DATE, "
                + "statut = 'RENDU' WHERE id = ? AND date_retour_effective IS NULL "
                + "RETURNING livre_id";
        String stockSql = "UPDATE livre SET exemplaires_disponibles = "
                + "exemplaires_disponibles + 1 WHERE id = ?";

        try (Connection conn = DatabaseConnection.get()) {
            conn.setAutoCommit(false);
            try {
                long livreId;
                try (PreparedStatement ps = conn.prepareStatement(retourSql)) {
                    ps.setLong(1, empruntId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException(
                                    "Emprunt introuvable ou déjà rendu : " + empruntId);
                        }
                        livreId = rs.getLong(1);
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(stockSql)) {
                    ps.setLong(1, livreId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'enregistrement du retour " + empruntId, e);
        }
    }

    @Override
    public Optional<Emprunt> findById(Long id) {
        String sql = "SELECT " + COLONNES + " FROM emprunt WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture de l'emprunt " + id, e);
        }
    }

    @Override
    public void save(Emprunt emprunt) {
        String sql = "INSERT INTO emprunt (livre_id, etudiant_id, date_emprunt, "
                + "date_retour_prevue, date_retour_effective, statut) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            remplirParametres(ps, emprunt);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'enregistrement de l'emprunt", e);
        }
    }

    @Override
    public void update(Long id, Emprunt emprunt) {
        String sql = "UPDATE emprunt SET livre_id = ?, etudiant_id = ?, date_emprunt = ?, "
                + "date_retour_prevue = ?, date_retour_effective = ?, statut = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            remplirParametres(ps, emprunt);
            ps.setLong(7, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la mise à jour de l'emprunt " + id, e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM emprunt WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression de l'emprunt " + id, e);
        }
    }

    // ------------------------------------------------------------------
    // C.3 : transaction "emprunter"
    // ------------------------------------------------------------------
    public void enregistrerEmprunt(long livreId, long etudiantId, int dureeJours) {
        String selectSql = "SELECT exemplaires_disponibles FROM livre WHERE id = ? FOR UPDATE";
        String updateSql = "UPDATE livre SET exemplaires_disponibles = "
                + "exemplaires_disponibles - 1 WHERE id = ?";
        String insertSql = "INSERT INTO emprunt (livre_id, etudiant_id, date_emprunt, "
                + "date_retour_prevue, statut) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.get()) {
            conn.setAutoCommit(false);
            try {
                // 1. Vérifier que le livre existe et qu'il reste un exemplaire
                int disponibles;
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setLong(1, livreId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new LivreIndisponibleException("Livre introuvable : " + livreId);
                        }
                        disponibles = rs.getInt(1);
                    }
                }
                if (disponibles <= 0) {
                    throw new LivreIndisponibleException(
                            "Aucun exemplaire disponible pour le livre " + livreId);
                }

                // 2. Décrémenter le stock
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setLong(1, livreId);
                    ps.executeUpdate();
                }

                // 3. Créer l'emprunt
                LocalDate aujourdHui = LocalDate.now();
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setLong(1, livreId);
                    ps.setLong(2, etudiantId);
                    ps.setDate(3, Date.valueOf(aujourdHui));
                    ps.setDate(4, Date.valueOf(aujourdHui.plusDays(dureeJours)));
                    ps.setString(5, "EN_COURS");
                    ps.executeUpdate();
                }

                conn.commit();   // tout a réussi : on valide les 3 étapes ensemble
            } catch (SQLException | RuntimeException e) {
                conn.rollback(); // une étape a échoué : on annule TOUT
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'enregistrement de l'emprunt", e);
        }
    }

    // ------------------------------------------------------------------
    // Méthodes utilitaires privées
    // ------------------------------------------------------------------
    private List<Emprunt> lister(String sql) {
        List<Emprunt> emprunts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                emprunts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture des emprunts", e);
        }
        return emprunts;
    }

    private void remplirParametres(PreparedStatement ps, Emprunt e) throws SQLException {
        ps.setLong(1, e.livreId());
        ps.setLong(2, e.etudiantId());
        ps.setDate(3, versSql(e.dateEmprunt()));
        ps.setDate(4, versSql(e.dateRetourPrevue()));
        ps.setDate(5, versSql(e.dateRetourEffective())); // peut être null
        ps.setString(6, statutVersTexte(e.statut()));
    }

    private static Date versSql(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }

    private static String statutVersTexte(StatutEmprunt statut) {
        return switch (statut) {
            case StatutEmprunt.EnCours e -> "EN_COURS";
            case StatutEmprunt.Rendu r -> "RENDU";
            case StatutEmprunt.EnRetard r -> "EN_RETARD";
        };
    }

    private Emprunt mapRow(ResultSet rs) throws SQLException {
        LocalDate prevue = rs.getDate("date_retour_prevue").toLocalDate();
        Date effectiveSql = rs.getDate("date_retour_effective");
        LocalDate effective = effectiveSql == null ? null : effectiveSql.toLocalDate();

        return new Emprunt(
                rs.getLong("id"),
                rs.getLong("livre_id"),
                rs.getLong("etudiant_id"),
                rs.getDate("date_emprunt").toLocalDate(),
                prevue,
                effective,
                calculerStatut(prevue, effective));
    }

    private static StatutEmprunt calculerStatut(LocalDate prevue, LocalDate effective) {
        if (effective != null) {
            return new StatutEmprunt.Rendu(effective, effective.isAfter(prevue));
        }
        LocalDate aujourdHui = LocalDate.now();
        if (prevue.isBefore(aujourdHui)) {
            return new StatutEmprunt.EnRetard(prevue, ChronoUnit.DAYS.between(prevue, aujourdHui));
        }
        return new StatutEmprunt.EnCours(prevue);
    }
}