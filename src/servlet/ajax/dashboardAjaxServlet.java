package servlet.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;

import GameEngine.GameEngine;
import GameEngine.PlayerLoadManager;
import models.Response;
import models.DerbyDatabase;
import models.Player;

public class dashboardAjaxServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Attempt to get the existing session
        HttpSession session = req.getSession(false);
        boolean sessionRecovered = false;
        
        // Check if session exists and has user_id
        if (session == null || session.getAttribute("user_id") == null) {
            // Try to recover session from cookies
            Cookie[] cookies = req.getCookies();
            Integer userId = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("user_id".equals(cookie.getName())) {
                        try {
                            userId = Integer.parseInt(cookie.getValue());
                            break;
                        } catch (NumberFormatException e) {
                            // Invalid cookie format, ignore
                        }
                    }
                }
            }
            
            if (userId != null) {
                // We found a user ID cookie, try to recover the session
                session = req.getSession(true); // Create new session
                session.setAttribute("user_id", userId);
                
                // Try to load the player associated with this user
                try {
                    // Get their most recent save or default player
                    PlayerLoadManager playerLoadManager = new PlayerLoadManager();
                    Player player = playerLoadManager.getDefaultPlayerForUser(userId);
                    
                    if (player != null) {
                        // Get the game engine
                        GameEngine gameEngine = (GameEngine)getServletContext()
                                                        .getAttribute("gameEngine");
                        
                        if (gameEngine != null) {
                            // Add player to engine and set player_id in session
                            int playerIndex = gameEngine.addPlayer(player);
                            session.setAttribute("player_id", playerIndex);
                            session.setAttribute("selectedClass", player.getPlayerType().toUpperCase());
                            sessionRecovered = true;
                            
                            // Send response indicating session was recovered
                            resp.setContentType("application/json");
                            resp.setCharacterEncoding("UTF-8");
                            String recoveryMessage = "{\"status\":\"recovered\",\"message\":\"Your session was automatically recovered.\"}";
                            resp.getWriter().write(recoveryMessage);
                            return;
                        }
                    }
                } catch (Exception e) {
                    // Session recovery failed
                    getServletContext().log("Session recovery failed", e);
                }
            }
            
            // If we got here, session recovery failed
            if (!sessionRecovered) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                               "Session expired or not found. Please log in again.");
                return;
            }
        }
        
        // Get player_id from session
        Integer playerId = (Integer) session.getAttribute("player_id");
        if (playerId == null) {
            // Try to load the default player for this user
            Integer userId = (Integer) session.getAttribute("user_id");
            if (userId != null) {
                try {
                    PlayerLoadManager playerLoadManager = new PlayerLoadManager();
                    Player player = playerLoadManager.getDefaultPlayerForUser(userId);
                    
                    if (player != null) {
                        // Get the game engine
                        GameEngine gameEngine = (GameEngine)getServletContext()
                                                        .getAttribute("gameEngine");
                        
                        // Add player to engine and set player_id in session
                        playerId = gameEngine.addPlayer(player);
                        session.setAttribute("player_id", playerId);
                        session.setAttribute("selectedClass", player.getPlayerType().toUpperCase());
                    }
                } catch (Exception e) {
                    getServletContext().log("Failed to load default player", e);
                }
            }
            
            // If still null, default to first player (index 0)
            if (playerId == null) {
                playerId = 0;
            }
        }
        
        // Set a persistent cookie for auto-recovery in the future
        Cookie userIdCookie = new Cookie("user_id", session.getAttribute("user_id").toString());
        userIdCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
        userIdCookie.setPath("/");
        resp.addCookie(userIdCookie);
        
        // Retrieve the GameEngine from servlet-context
        GameEngine gameEngine = (GameEngine)getServletContext()
                                        .getAttribute("gameEngine");
        if (gameEngine == null) {
            throw new ServletException("Game Engine doesn't exist");
        }
        
        // Process input & render with player_id
        String input = req.getParameter("input");
        gameEngine.processInput(input, playerId);
        
        Response updatedResponse = gameEngine.display(playerId);
        String jsonResponse = updatedResponse.toJson();
        
        // Send JSON response
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.write(jsonResponse);
        }
    }
}
