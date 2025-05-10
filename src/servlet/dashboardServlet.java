package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import GameEngine.GameEngine;
import models.Response;
import models.DerbyDatabase;

public class dashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(true);

        // ←— ADDED return after redirect
        if (session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        GameEngine gameEngine = (GameEngine)getServletContext()
                                       .getAttribute("gameEngine");
        if (gameEngine == null) {
            throw new IllegalStateException("Game Engine doesn't exist");
        }

        if (!gameEngine.getRunning()) {
            gameEngine.startWithoutPlayer();
        }

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
                // If the PLAYER table doesn't exist yet, ignore...
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

        // Render game
        Response response = gameEngine.display();
        req.setAttribute("response", response);
        req.getRequestDispatcher("/_view/Dashboard.jsp")
           .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String cls = req.getParameter("selectedClass");
        if (cls != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("selectedClass", cls);

            GameEngine engine = new GameEngine();
            engine.startWithoutPlayer();
            engine.loadPlayerOfClass(cls);
            session.setAttribute("gameEngine", engine);

            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("gameEngine") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                           "Session expired or not found.");
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
        req.getRequestDispatcher("/_view/Dashboard.jsp")
           .forward(req, resp);
    }
}
