package com.bibliotech.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RaceConditionDemo {

    private static final int RONDES = 100;

    public static void main(String[] args) throws Exception {
        long livreId = creerLivreDeTest();      // livre à 1 seul exemplaire
        EmpruntDAO dao = new EmpruntDAO();
        int rondesAnormales = 0;

        try {
            for (int ronde = 1; ronde <= RONDES; ronde++) {
                reinitialiser(livreId);          // stock remis à 1, emprunts effacés

                CountDownLatch topDepart = new CountDownLatch(1);
                AtomicInteger reussis = new AtomicInteger();
                ExecutorService pool = Executors.newFixedThreadPool(2);

                for (long etudiantId = 1; etudiantId <= 2; etudiantId++) {   // Alice (1) et Bob (2)
                    long id = etudiantId;
                    pool.submit(() -> {
                        try {
                            topDepart.await();                    // les 2 threads attendent ici
                            dao.enregistrerEmprunt(livreId, id, 14);
                            reussis.incrementAndGet();
                        } catch (LivreIndisponibleException e) {
                            // attendu pour celui qui arrive trop tard
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                topDepart.countDown();                            // top départ simultané
                pool.shutdown();
                pool.awaitTermination(10, TimeUnit.SECONDS);

                int stock = lireStock(livreId);
                if (reussis.get() > 1 || stock < 0) {
                    rondesAnormales++;
                    System.out.println("Ronde " + ronde + " : " + reussis.get()
                            + " emprunts réussis pour 1 exemplaire, stock final = " + stock);
                }
            }
        } finally {
            supprimerLivreDeTest(livreId);
        }

        System.out.println("=> " + rondesAnormales + " ronde(s) anormale(s) sur " + RONDES);
    }

    // ------------------------ utilitaires SQL ------------------------
    private static long creerLivreDeTest() throws SQLException {
        String sql = "INSERT INTO livre (titre, auteur, annee_publication, "
                + "exemplaires_total, exemplaires_disponibles) "
                + "VALUES ('Livre test race', 'Test', 2000, 1, 1) RETURNING id";
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static void reinitialiser(long livreId) throws SQLException {
        executer("DELETE FROM emprunt WHERE livre_id = ?", livreId);
        executer("UPDATE livre SET exemplaires_disponibles = 1 WHERE id = ?", livreId);
    }

    private static int lireStock(long livreId) throws SQLException {
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT exemplaires_disponibles FROM livre WHERE id = ?")) {
            ps.setLong(1, livreId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static void supprimerLivreDeTest(long livreId) throws SQLException {
        executer("DELETE FROM emprunt WHERE livre_id = ?", livreId);
        executer("DELETE FROM livre WHERE id = ?", livreId);
    }

    private static void executer(String sql, long param) throws SQLException {
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, param);
            ps.executeUpdate();
        }
    }
}