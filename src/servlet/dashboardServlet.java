package servlet;

import java.io.IOException;
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
        
        // Get the initial game state.
        Response response = gameEngine.display();
        
        // Set the response as a request attribute for the JSP.
        req.setAttribute("response", response);
        req.getRequestDispatcher("/game.jsp").forward(req, resp);
    }
	
	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
		// Retrieve the GameEngine from session.
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("gameEngine") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired or not found.");
            return;
        }
        GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");

        // Process the input from the request.
        String input = req.getParameter("input");
        gameEngine.processInput(input);

        // Get updated game state.
        Response updatedResponse = gameEngine.display();

        req.setAttribute("response", updatedResponse);
        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}