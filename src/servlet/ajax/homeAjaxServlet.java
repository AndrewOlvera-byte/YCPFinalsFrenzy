package servlet.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import GameEngine.PlayerLoadManager;
import GameEngine.PlayerLoadManager.GameState;

public class homeAjaxServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Check if user is logged in
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        int userId = (int) session.getAttribute("user_id");
        
        // Get save slots for the user
        PlayerLoadManager playerLoadManager = new PlayerLoadManager();
        List<GameState> saveSlots = playerLoadManager.getSaveSlots(userId);
        
        // Create JSON response manually (since we're not using external JSON libraries)
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"saveSlots\":[");
        
        for (int i = 0; i < saveSlots.size(); i++) {
            GameState saveSlot = saveSlots.get(i);
            
            if (saveSlot != null && !saveSlot.isEmpty()) {
                jsonBuilder.append("{");
                jsonBuilder.append("\"stateId\":").append(saveSlot.getStateId()).append(",");
                jsonBuilder.append("\"playerId\":").append(saveSlot.getPlayerId()).append(",");
                jsonBuilder.append("\"currentRoom\":").append(saveSlot.getCurrentRoom()).append(",");
                jsonBuilder.append("\"playerHp\":").append(saveSlot.getPlayerHp()).append(",");
                jsonBuilder.append("\"saveName\":\"").append(escapeJson(saveSlot.getSaveName())).append("\",");
                jsonBuilder.append("\"isEmpty\":false");
                jsonBuilder.append("}");
            } else {
                // Add empty slot
                jsonBuilder.append("{");
                jsonBuilder.append("\"isEmpty\":true");
                jsonBuilder.append("}");
            }
            
            // Add comma between items except for the last one
            if (i < saveSlots.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        
        jsonBuilder.append("]}");
        
        // Send response
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.write(jsonBuilder.toString());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Check if user is logged in
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"User not authenticated\"}");
            return;
        }
        
        String action = req.getParameter("action");
        
        if ("initNewPlayer".equals(action)) {
            // Return available player classes
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{\"playerClasses\":[");
            jsonResponse.append("\"Warrior\",");
            jsonResponse.append("\"Mage\",");
            jsonResponse.append("\"Rogue\"");
            jsonResponse.append("]}");
            
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(jsonResponse.toString());
        } else {
            // Invalid action
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Invalid action\"}");
        }
    }
    
    /** Helper to escape double quotes in JSON strings */
    private String escapeJson(String s) {
        return (s == null) ? "" : s.replace("\"", "\\\"");
    }
}
