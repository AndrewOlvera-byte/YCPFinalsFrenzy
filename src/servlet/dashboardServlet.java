package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import GameEngine.GameEngine;
import models.Response;
import models.DerbyDatabase;

public class dashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(true);

        // Auto-resume existing player if session lost
        if (session.getAttribute("selectedClass") == null) {
            try (Connection conn = DerbyDatabase.getConnection()) {
                String sql = "SELECT player_type FROM PLAYER WHERE player_id = 1";
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String existingClass = rs.getString("player_type");
                        session.setAttribute("selectedClass", existingClass);
                    }
                }
            } catch (SQLException e) {
                // If the PLAYER table doesn't exist yet, ignore and let user select a class
                if (!"42X05".equals(e.getSQLState())) {
                    throw new ServletException("Error loading existing player", e);
                }
            }
        }

        // If still no class selected, show selection screen
        if (session.getAttribute("selectedClass") == null) {
            req.getRequestDispatcher("/_view/ClassSelect.jsp")
               .forward(req, resp);
            return;
        }

        // Retrieve or create game engine
        GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");
        if (gameEngine == null) {
            gameEngine = new GameEngine();
            gameEngine.start();
            session.setAttribute("gameEngine", gameEngine);
        }

        // Render game
        Response response = gameEngine.display();
        req.setAttribute("response", response);
        req.getRequestDispatcher("/_view/Dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String cls = req.getParameter("selectedClass");
        if (cls != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("selectedClass", cls);

            GameEngine engine = new GameEngine();
            // full initialization runs DDL, seeds CSVs, loads data (including quests)
            engine.start();
            // override player by selected class
            engine.loadPlayerOfClass(cls);
            session.setAttribute("gameEngine", engine);

            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("gameEngine") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired or not found.");
            return;
        }

        String restart = req.getParameter("restart");
        if ("true".equals(restart)) {
            GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");
            if (gameEngine != null) {
                gameEngine.reset();
            }
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");
        String input = req.getParameter("input");
        if (input != null && !input.trim().isEmpty()) {
            gameEngine.processInput(input);
        }

        Response updatedResponse = gameEngine.display();
        req.setAttribute("response", updatedResponse);
        req.getRequestDispatcher("/_view/Dashboard.jsp").forward(req, resp);
    }
}
