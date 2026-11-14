package com.bibliotech.dao;

import com.bibliotech.model.Livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LivreDAO implements GenericDAO<Livre, Long> {

    private static final String COLONNES =
            "id, titre, auteur, annee_publication, exemplaires_total, exemplaires_disponibles";

    @Override
    public List<Livre> findAll() {
        String sql = "SELECT " + COLONNES + " FROM livre ORDER BY id";
        List<Livre> livres = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                livres.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture des livres", e);
        }
        return livres;
    }

    public List<Livre> rechercherParTitre(String terme) {
        String sql = "SELECT " + COLONNES + " FROM livre WHERE titre ILIKE ? ORDER BY id";
        List<Livre> livres = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + terme + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    livres.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche de livres", e);
        }
        return livres;
    }

    @Override
    public Optional<Livre> findById(Long id) {
        String sql = "SELECT " + COLONNES + " FROM livre WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la lecture du livre " + id, e);
        }
    }

    @Override
    public void save(Livre livre) {
        String sql = "INSERT INTO livre (titre, auteur, annee_publication, "
                + "exemplaires_total, exemplaires_disponibles) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, livre.titre());
            ps.setString(2, livre.auteur());
            ps.setInt(3, livre.anneePublication());
            ps.setInt(4, livre.exemplairesTotal());
            ps.setInt(5, livre.exemplairesDisponibles());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'enregistrement du livre", e);
        }
    }

    @Override
    public void update(Long id, Livre livre) {
        String sql = "UPDATE livre SET titre = ?, auteur = ?, annee_publication = ?, "
                + "exemplaires_total = ?, exemplaires_disponibles = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, livre.titre());
            ps.setString(2, livre.auteur());
            ps.setInt(3, livre.anneePublication());
            ps.setInt(4, livre.exemplairesTotal());
            ps.setInt(5, livre.exemplairesDisponibles());
            ps.setLong(6, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la mise à jour du livre " + id, e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM livre WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression du livre " + id, e);
        }
    }

    private Livre mapRow(ResultSet rs) throws SQLException {
        return new Livre(
                rs.getLong("id"),
                rs.getString("titre"),
                rs.getString("auteur"),
                rs.getInt("annee_publication"),
                rs.getInt("exemplaires_total"),
                rs.getInt("exemplaires_disponibles"));
    }
}