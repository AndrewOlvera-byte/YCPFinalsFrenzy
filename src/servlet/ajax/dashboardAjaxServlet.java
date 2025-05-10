package servlet.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import GameEngine.GameEngine;
import models.Response;

public class dashboardAjaxServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 1) ensure user is logged in
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                           "Session expired or not found.");
            return;
        }
        
        // 2) retrieve the GameEngine from servlet-context (same as dashboardServlet#doGet)
        GameEngine gameEngine = (GameEngine)getServletContext()
                                        .getAttribute("gameEngine");
        if (gameEngine == null) {
            throw new ServletException("Game Engine doesn't exist");
        }
        
        // 3) process input & render
        String input = req.getParameter("input");
        gameEngine.processInput(input);
        
        Response updatedResponse = gameEngine.display();
        String jsonResponse    = updatedResponse.toJson();
        
        // 4) send JSON back
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.write(jsonResponse);
        }
    }
}
