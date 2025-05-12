package servlet.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

// We'll use the built-in JSON functionality in the Response class
import models.Response;

public class SessionDataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired");
            return;
        }
        
        // Get session data
        Integer userId = (Integer) session.getAttribute("user_id");
        Integer playerId = (Integer) session.getAttribute("player_id");
        
        // Create response object
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);
        data.put("player_id", playerId);
        
        // Convert to JSON using Response utility
        String jsonResponse = Response.toJson(data);
        
        // Send JSON response
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.write(jsonResponse);
        }
    }
} 