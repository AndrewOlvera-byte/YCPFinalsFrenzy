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
        // Retrieve the GameEngine instance from session.
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("gameEngine") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired or not found.");
            return;
        }
        GameEngine gameEngine = (GameEngine) session.getAttribute("gameEngine");
        
        // Process the user input.
        String input = req.getParameter("input");
        gameEngine.processInput(input);
        
        // Get updated game state.
        Response updatedResponse = gameEngine.display();
        
        // Use the toJson() method from Response to convert it to a JSON string.
        String jsonResponse = updatedResponse.toJson();
        
        // Send the JSON string as the response.
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.write(jsonResponse);
        out.flush();
    }
}