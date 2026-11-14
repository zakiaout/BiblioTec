package com.bibliotech.web;

import com.bibliotech.dao.EmpruntDAO;
import com.bibliotech.dao.LivreDAO;
import com.bibliotech.model.Emprunt;
import com.bibliotech.model.Livre;
import com.bibliotech.model.PenaliteCalculator;
import com.bibliotech.model.StatutEmprunt;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/emprunts/*")
public class EmpruntServlet extends HttpServlet {

    private final EmpruntDAO empruntDAO = new EmpruntDAO();
    private final LivreDAO livreDAO = new LivreDAO();

    // GET /emprunts : liste des emprunts en cours (retards inclus)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (decouper(req.getPathInfo()).length != 0) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Map<Long, String> titres = new HashMap<>();
        for (Livre livre : livreDAO.findAll()) {
            titres.put(livre.id(), livre.titre());
        }

        List<EmpruntLigne> lignes = empruntDAO.findEnCours().stream()
                .map(e -> versLigne(e, titres))
                .toList();

        req.setAttribute("emprunts", lignes);
        req.getRequestDispatcher("/WEB-INF/emprunts/list.jsp").forward(req, resp);
    }

    // POST /emprunts/{id}/retour : enregistre un retour puis redirige (PRG)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String[] parts = decouper(req.getPathInfo());

        if (parts.length == 2 && parts[1].equals("retour") && parseId(parts[0]) != null) {
            try {
                empruntDAO.enregistrerRetour(parseId(parts[0]));
                resp.sendRedirect(req.getContextPath() + "/emprunts");   // PRG
            } catch (IllegalStateException e) {
                resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static EmpruntLigne versLigne(Emprunt e, Map<Long, String> titres) {
        return new EmpruntLigne(
                e.id(),
                titres.getOrDefault(e.livreId(), "Livre #" + e.livreId()),
                e.etudiantId(),
                e.dateEmprunt(),
                e.dateRetourPrevue(),
                libelle(e.statut()),
                PenaliteCalculator.calculerPenalite(e.statut()));
    }

    private static String libelle(StatutEmprunt statut) {
        return switch (statut) {
            case StatutEmprunt.EnCours e -> "En cours";
            case StatutEmprunt.Rendu r -> "Rendu";
            case StatutEmprunt.EnRetard r -> "En retard (" + r.joursDeRetard() + " j)";
        };
    }

    private static Long parseId(String texte) {
        try {
            return Long.parseLong(texte);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String[] decouper(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            return new String[0];
        }
        return pathInfo.substring(1).split("/");
    }
}