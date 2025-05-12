package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import GameEngine.GameEngine;
import models.Player;

public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        
        if (session != null) {
            // Get player and gameEngine references for cleanup
            Integer playerId = (Integer) session.getAttribute("player_id");
            Integer userId = (Integer) session.getAttribute("user_id");
            Integer playerDbId = (Integer) session.getAttribute("player_db_id");
            
            // Save player state before logout if possible
            if (playerId != null) {
                GameEngine gameEngine = (GameEngine) getServletContext().getAttribute("gameEngine");
                if (gameEngine != null) {
                    // Save player state before removing session
                    try {
                        Player player = gameEngine.getPlayerById(playerId);
                        if (player != null) {
                            System.out.println("Saving state for player " + playerId + " before logout");
                            gameEngine.saveAllPlayersState();
                        }
                    } catch (Exception e) {
                        // Log but continue with logout
                        System.out.println("Error saving player state during logout: " + e.getMessage());
                    }
                }
            }
            
            // Invalidate session - this removes all session attributes
            session.invalidate();
            System.out.println("Session invalidated for user " + userId);
        }
        
        // Delete all cookies
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0); // Delete the cookie
                resp.addCookie(cookie);
            }
        }
        
        // Redirect to login page
        resp.sendRedirect(req.getContextPath() + "/login");
    }
} 