package com.bibliotech;

import com.bibliotech.dao.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

@WebServlet("/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain");

        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement("SELECT version()");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String version = rs.getString(1);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().println("OK · " + version);
            }

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().println("KO · " + e.getMessage());
        }
    }
}