package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import GameEngine.GameEngine;
import models.Response;

public class dashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Create or retrieve the session and GameEngine instance.
        HttpSession session = req.getSession(true);
        GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");
        if (gameEngine == null) {
            gameEngine = new GameEngine();
            gameEngine.start();
            session.setAttribute("gameEngine", gameEngine);
        }

        // Get the current game state and forward to JSP.
        Response response = gameEngine.display();
        req.setAttribute("response", response);
        req.getRequestDispatcher("/_view/Dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) Retrieve existing session (do NOT create a new one)
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("gameEngine") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired or not found.");
            return;
        }

        // 2) Handle the Restart button before any game-input processing
        String restart = req.getParameter("restart");
        if ("true".equals(restart)) {
            // Remove the old engine and redirect back to GET /dashboard
        	GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");
        	if(gameEngine != null) {
        		gameEngine.reset();
        	}
        	
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        // 3) Normal game-input flow
        GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");

        // Only process non-null, non-empty commands
        String input = req.getParameter("input");
        if (input != null && !input.trim().isEmpty()) {
            gameEngine.processInput(input);
        }

        // 4) Render the updated game state
        Response updatedResponse = gameEngine.display();
        req.setAttribute("response", updatedResponse);
        req.getRequestDispatcher("/_view/Dashboard.jsp").forward(req, resp);
    }
}
