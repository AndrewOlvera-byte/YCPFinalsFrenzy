package servlet;

import java.io.IOException;
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

public class dashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(true);

        // Check if user is logged in
        if (session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Get the user ID
        int userId = (int) session.getAttribute("user_id");
        
        // Get or create a player ID
        Integer playerId = (Integer) session.getAttribute("player_id");
        
        // Get the game engine
        GameEngine gameEngine = (GameEngine)getServletContext()
                                       .getAttribute("gameEngine");
        if (gameEngine == null) {
            throw new IllegalStateException("Game Engine doesn't exist");
        }

        // Ensure the game engine is running
        if (!gameEngine.getRunning()) {
            gameEngine.startWithoutPlayer();
        }
        
        // If we don't have a player_id in session but coming from home (which should have set it)
        // This handles possible consistency issues between home and dashboard
        if (playerId == null) {
            // Try to get the default player for this user
            PlayerLoadManager playerLoadManager = new PlayerLoadManager();
            Player player = playerLoadManager.getDefaultPlayerForUser(userId);
            
            if (player != null) {
                // Add player to game engine and record index in session
                playerId = gameEngine.addPlayer(player);
                session.setAttribute("player_id", playerId);
                
                // Also store the player's database ID for consistent reference
                session.setAttribute("player_db_id", player.getId());
                
                session.setAttribute("selectedClass", player.getPlayerType().toUpperCase());
                
                // Set game engine's current room to match the player's room
                if (player.getCurrentRoomNum() >= 0) {
                    gameEngine.setCurrentRoomNum(player.getCurrentRoomNum());
                } else {
                    // Default to room 0 if player doesn't have a room
                    gameEngine.setCurrentRoomNum(0);
                }
            }
        } else {
            // Ensure currentRoomNum in GameEngine matches player's room
            Player player = gameEngine.getPlayerById(playerId);
            
            // If player not found or verification fails, attempt to recover
            if (player == null || player.getUserId() != userId) {
                // Player validation failed - clean up the session and attempt to load the correct one
                System.out.println("Player validation failed: session player_id " + playerId + 
                    " doesn't match user " + userId);
                
                // Clear invalid player_id from session
                session.removeAttribute("player_id");
                session.removeAttribute("player_db_id");
                
                // Try to load the proper player for this user
                PlayerLoadManager playerLoadManager = new PlayerLoadManager();
                Player correctPlayer = playerLoadManager.getDefaultPlayerForUser(userId);
                
                if (correctPlayer != null) {
                    // Add correct player to game engine
                    playerId = gameEngine.addPlayer(correctPlayer);
                    session.setAttribute("player_id", playerId);
                    session.setAttribute("player_db_id", correctPlayer.getId());
                    player = correctPlayer;
                } else {
                    // No player found for this user, redirect to home to create one
                    resp.sendRedirect(req.getContextPath() + "/home");
                    return;
                }
            }
            
            if (player != null && player.getCurrentRoomNum() >= 0) {
                gameEngine.setCurrentRoomNum(player.getCurrentRoomNum());
                
                // Ensure we have the database ID stored in session
                if (session.getAttribute("player_db_id") == null) {
                    session.setAttribute("player_db_id", player.getId());
                }
                
                // Force a save of the player state when arriving at dashboard
                // This ensures equipment data is properly saved
                gameEngine.saveAllPlayersState();
            }
        }

        // At this point if we have a player_id but not a selectedClass, set it
        if (playerId != null && session.getAttribute("selectedClass") == null) {
            // Get the player from the game engine
            Player player = gameEngine.getPlayerById(playerId);
            if (player != null) {
                session.setAttribute("selectedClass", player.getPlayerType().toUpperCase());
            }
        }

        // If we STILL have no class selected after recovery attempts, show selection screen
        if (session.getAttribute("selectedClass") == null) {
            req.getRequestDispatcher("/_view/ClassSelect.jsp")
               .forward(req, resp);
            return;
        }

        // Set a cookie to help with session recovery
        Cookie userIdCookie = new Cookie("user_id", String.valueOf(userId));
        userIdCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
        userIdCookie.setPath("/");
        resp.addCookie(userIdCookie);
        
        // Render game with player ID if available, otherwise use default player (index 0)
        Response response;
        if (playerId != null) {
            response = gameEngine.display(playerId);
        } else {
            response = gameEngine.display();
        }
        
        req.setAttribute("response", response);
        req.getRequestDispatcher("/_view/Dashboard.jsp")
           .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Handle class selection form submission
        String cls = req.getParameter("selectedClass");
        if (cls != null) {
            session.setAttribute("selectedClass", cls);

            // Get existing game engine
            GameEngine gameEngine = (GameEngine)getServletContext()
                                        .getAttribute("gameEngine");
            if (gameEngine == null) {
                throw new IllegalStateException("Game Engine doesn't exist");
            }
            
            // Create a new player of this class and add it to the engine
            gameEngine.loadPlayerOfClass(cls);
            
            // Get the player (should be the last one added)
            int playerIndex = gameEngine.getPlayers().size() - 1;
            Player newPlayer = gameEngine.getPlayerById(playerIndex);
            
            session.setAttribute("player_id", playerIndex);
            
            // Also store the database ID for consistent reference
            if (newPlayer != null) {
                session.setAttribute("player_db_id", newPlayer.getId());
            }
            
            // Save state before redirecting
            gameEngine.saveAllPlayersState();

            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        // Handle game restart
        String restart = req.getParameter("restart");
        if ("true".equals(restart)) {
            GameEngine gameEngine = (GameEngine)getServletContext()
                                        .getAttribute("gameEngine");
            if (gameEngine != null) {
                gameEngine.reset();
                gameEngine.startWithoutPlayer();
                
                // Clear player_id from session since we reset the game
                session.removeAttribute("player_id");
            }
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        // Process game input commands
        String input = req.getParameter("input");
        if (input != null && !input.trim().isEmpty()) {
            GameEngine gameEngine = (GameEngine)getServletContext()
                                        .getAttribute("gameEngine");
            
            // Get player_id from session
            Integer playerId = (Integer) session.getAttribute("player_id");
            if (playerId == null) {
                // Default to first player if not set
                playerId = 0;
            }
            
            // Process input with player ID
            gameEngine.processInput(input, playerId);
            
            // Explicitly save game state after processing input
            gameEngine.saveAllPlayersState();
            
            // Display response for this player
            Response updatedResponse = gameEngine.display(playerId);
            req.setAttribute("response", updatedResponse);
            req.getRequestDispatcher("/_view/Dashboard.jsp")
               .forward(req, resp);
        } else {
            // No input, just redisplay
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        }
    }
}
