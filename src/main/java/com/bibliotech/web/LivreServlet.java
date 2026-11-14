package com.bibliotech.web;

import com.bibliotech.dao.DaoException;
import com.bibliotech.dao.LivreDAO;
import com.bibliotech.model.ChampInvalideException;
import com.bibliotech.model.Livre;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@WebServlet("/livres/*")
public class LivreServlet extends HttpServlet {

    private static final String[] CHAMPS = {
            "titre", "auteur", "anneePublication", "exemplairesTotal", "exemplairesDisponibles"
    };

    private final LivreDAO livreDAO = new LivreDAO();

    // ------------------------------------------------------------------
    // GET : lecture seule
    // ------------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String[] parts = decouper(req.getPathInfo());

        if (parts.length == 0) {                                   // GET /livres (?q=...)
            String q = req.getParameter("q");
            List<Livre> livres = (q == null || q.isBlank())
                    ? livreDAO.findAll()
                    : livreDAO.rechercherParTitre(q.trim());
            req.setAttribute("livres", livres);
            req.getRequestDispatcher("/WEB-INF/livres/list.jsp").forward(req, resp);

        } else if (parts.length == 1 && parts[0].equals("nouveau")) { // GET /livres/nouveau else if (parts.length == 1 && parts[0].equals("nouveau")) { // GET /livres/nouveau
            afficherFormulaire(req, resp, null, valeursVides(), new HashMap<>());

        } else if (parts.length == 1) {                            // GET /livres/{id}
            Optional<Livre> livre = chercher(parts[0]);
            if (livre.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("livre", livre.get());
            req.getRequestDispatcher("/WEB-INF/livres/detail.jsp").forward(req, resp);

        } else if (parts.length == 2 && parts[1].equals("edit")) { // GET /livres/{id}/edit
            Optional<Livre> livre = chercher(parts[0]);
            if (livre.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            afficherFormulaire(req, resp, livre.get().id(),
                    versValeurs(livre.get()), new HashMap<>());

        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ------------------------------------------------------------------
    // POST : création, modification, suppression (toujours suivies d'un PRG)
    // ------------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8"); // pour bien lire les accents du formulaire
        String[] parts = decouper(req.getPathInfo());

        if (parts.length == 0) {                                    // POST /livres
            creer(req, resp);

        } else if (parts.length == 1 && parseId(parts[0]) != null) { // POST /livres/{id}
            modifier(req, resp, parseId(parts[0]));

        } else if (parts.length == 2 && parts[1].equals("delete")
                && parseId(parts[0]) != null) {                      // POST /livres/{id}/delete
            supprimer(req, resp, parseId(parts[0]));

        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void creer(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Map<String, String> valeurs = lireValeurs(req);
        try {
            livreDAO.save(construireLivre(0L, valeurs));
            resp.sendRedirect(req.getContextPath() + "/livres");   // PRG : 302, pas de forward
        } catch (ChampInvalideException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put(e.getChamp(), e.getMessage());
            afficherFormulaire(req, resp, null, valeurs, errors);   // formulaire réaffiché
        }
    }

    private void modifier(HttpServletRequest req, HttpServletResponse resp, Long id)
            throws ServletException, IOException {
        if (livreDAO.findById(id).isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Map<String, String> valeurs = lireValeurs(req);
        try {
            livreDAO.update(id, construireLivre(id, valeurs));
            resp.sendRedirect(req.getContextPath() + "/livres");   // PRG
        } catch (ChampInvalideException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put(e.getChamp(), e.getMessage());
            afficherFormulaire(req, resp, id, valeurs, errors);
        }
    }

    private void supprimer(HttpServletRequest req, HttpServletResponse resp, Long id)
            throws IOException {
        try {
            livreDAO.delete(id);
            resp.sendRedirect(req.getContextPath() + "/livres");   // PRG
        } catch (DaoException e) {
            // cas typique : le livre est référencé par des emprunts (clé étrangère)
            resp.sendError(HttpServletResponse.SC_CONFLICT,
                    "Suppression impossible : ce livre est lié à des emprunts.");
        }
    }

    // ------------------------------------------------------------------
    // Méthodes utilitaires
    // ------------------------------------------------------------------
    private void afficherFormulaire(HttpServletRequest req, HttpServletResponse resp, Long id,
                                    Map<String, String> valeurs, Map<String, String> errors)
            throws ServletException, IOException {
        req.setAttribute("form", valeurs);
        req.setAttribute("errors", errors);
        req.setAttribute("formTitre", id == null ? "Ajouter un livre" : "Modifier le livre");
        req.setAttribute("formAction",
                req.getContextPath() + "/livres" + (id == null ? "" : "/" + id));
        req.getRequestDispatcher("/WEB-INF/livres/form.jsp").forward(req, resp);
    }

    private Livre construireLivre(long id, Map<String, String> v) {
        int annee = parseEntier(v.get("anneePublication"), "anneePublication",
                "L'année de publication doit être un nombre entier");
        int total = parseEntier(v.get("exemplairesTotal"), "exemplairesTotal",
                "Le nombre total d'exemplaires doit être un nombre entier");
        int dispo = parseEntier(v.get("exemplairesDisponibles"), "exemplairesDisponibles",
                "Le nombre d'exemplaires disponibles doit être un nombre entier");
        return new Livre(id, v.get("titre"), v.get("auteur"), annee, total, dispo);
    }

    private static int parseEntier(String texte, String champ, String message) {
        if (texte == null) {
            throw new ChampInvalideException(champ, message);
        }
        try {
            return Integer.parseInt(texte.trim());
        } catch (NumberFormatException e) {
            throw new ChampInvalideException(champ, message);
        }
    }

    private static Map<String, String> lireValeurs(HttpServletRequest req) {
        Map<String, String> valeurs = new LinkedHashMap<>();
        for (String champ : CHAMPS) {
            String valeur = req.getParameter(champ);
            valeurs.put(champ, valeur == null ? "" : valeur);
        }
        return valeurs;
    }

    private static Map<String, String> valeursVides() {
        Map<String, String> valeurs = new LinkedHashMap<>();
        for (String champ : CHAMPS) {
            valeurs.put(champ, "");
        }
        return valeurs;
    }

    private static Map<String, String> versValeurs(Livre l) {
        Map<String, String> valeurs = new LinkedHashMap<>();
        valeurs.put("titre", l.titre());
        valeurs.put("auteur", l.auteur());
        valeurs.put("anneePublication", String.valueOf(l.anneePublication()));
        valeurs.put("exemplairesTotal", String.valueOf(l.exemplairesTotal()));
        valeurs.put("exemplairesDisponibles", String.valueOf(l.exemplairesDisponibles()));
        return valeurs;
    }

    private Optional<Livre> chercher(String idTexte) {
        Long id = parseId(idTexte);
        return id == null ? Optional.empty() : livreDAO.findById(id);
    }

    private static Long parseId(String texte) {
        try {
            return Long.parseLong(texte);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** "/12/edit" devient ["12", "edit"] ; null ou "/" devient []. */
    private static String[] decouper(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            return new String[0];
        }
        return pathInfo.substring(1).split("/");
    }
}