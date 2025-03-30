package servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import GameEngine.GameEngine;
import init.DatabaseInitializer;
import init.DataSeeder;
import models.Player;
import models.Response;
import orm.OrmManager;

public class dashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static boolean seeded = false;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	
    	OrmManager orm = new OrmManager("YCPGameDB");
    	
        HttpSession session = req.getSession(true);
        GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");

        if (gameEngine == null) {
            try {
                
            	if (!seeded) { 
                    DatabaseInitializer.initialize(orm);
                    DataSeeder.seed(orm);
                    seeded = true;
                }

                gameEngine = new GameEngine();
                gameEngine.start(orm);
                session.setAttribute("gameEngine", gameEngine);

            } catch (Exception e) {
                throw new ServletException("Database initialization failed", e);
            }
        }

        Response response = gameEngine.display();
        req.setAttribute("response", response);
        req.getRequestDispatcher("/_view/Dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("gameEngine") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired or not found.");
            return;
        }

        GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");
        String input = req.getParameter("input");
        gameEngine.processInput(input);
        
        OrmManager orm = null;
		orm = new OrmManager("YCPGameDB");
        gameEngine.saveGameState(orm);

        Response updatedResponse = gameEngine.display();
        req.setAttribute("response", updatedResponse);
        req.getRequestDispatcher("/_view/Dashboard.jsp").forward(req, resp);
    }
}
