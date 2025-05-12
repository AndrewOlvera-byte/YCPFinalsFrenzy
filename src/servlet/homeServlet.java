package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;

import GameEngine.GameEngine;
import GameEngine.PlayerLoadManager;
import GameEngine.PlayerLoadManager.GameState;
import java.util.List;

public class homeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Check if user is logged in
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        int userId = (int) session.getAttribute("user_id");
        
        // Get player save slots
        PlayerLoadManager playerLoadManager = new PlayerLoadManager();
        List<GameState> saveSlots = playerLoadManager.getSaveSlots(userId);
        req.setAttribute("saveSlots", saveSlots);
        
        req.getRequestDispatcher("/_view/Home.jsp").forward(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Check if user is logged in
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        int userId = (int) session.getAttribute("user_id");
        String action = req.getParameter("action");
        
        PlayerLoadManager playerLoadManager = new PlayerLoadManager();
        
        // Get the GameEngine from ServletContext, which is set up by GameEngineContextListener
        GameEngine gameEngine = (GameEngine) getServletContext().getAttribute("gameEngine");
        if (gameEngine == null) {
            throw new ServletException("GameEngine not available");
        }
        
        if ("loadSaveSlot".equals(action)) {
            // Load existing player from save slot
            int playerId = Integer.parseInt(req.getParameter("playerId"));
            int currentRoom = Integer.parseInt(req.getParameter("currentRoom"));
            
            // Load player into GameEngine
            models.Player player = playerLoadManager.loadPlayer(playerId);
            if (player != null) {
                // Make sure the game engine is running without resetting it
                if (!gameEngine.getRunning()) {
                    // Start the game engine if it's not running
                    gameEngine.startWithoutPlayer();
                }
                
                // Ensure player equipment is properly loaded
                try {
                    java.sql.Connection conn = models.DerbyDatabase.getConnection();
                    // Load player equipment
                    models.GameStateManager.loadPlayerEquipment(conn, player);
                    
                    // Also load the latest player stats from game state to be safe
                    String sql = "SELECT player_hp, damage_multi, attack_boost, defense_boost " +
                                "FROM GAME_STATE WHERE player_id = ?";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, player.getId());
                        try (java.sql.ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                player.setHp(rs.getInt("player_hp"));
                                player.setdamageMulti(rs.getDouble("damage_multi"));
                                
                                if (rs.getObject("attack_boost") != null) {
                                    player.setAttackBoost(rs.getInt("attack_boost"));
                                }
                                if (rs.getObject("defense_boost") != null) {
                                    player.setdefenseBoost(rs.getInt("defense_boost"));
                                }
                                
                                System.out.println("homeServlet: updated player stats from GAME_STATE - HP: " + 
                                                  player.getHp() + ", Attack: " + player.getAttackBoost());
                            }
                        }
                    }
                    
                    conn.close();
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
                
                // Check if this player is already in the players ArrayList
                int existingIndex = gameEngine.findPlayerIndexById(player.getId());
                int playerIndex;
                
                if (existingIndex >= 0) {
                    // Player already exists, update the player and use existing index
                    gameEngine.getPlayers().set(existingIndex, player);
                    playerIndex = existingIndex;
                } else {
                    // Add player to the GameEngine's players ArrayList
                    playerIndex = gameEngine.addPlayer(player);
                }
                
                // Set player_id in session to the index in the players ArrayList
                session.setAttribute("player_id", playerIndex);
                
                // Also store the player's database ID for consistent reference
                session.setAttribute("player_db_id", player.getId());
                
                // Store selected class in session based on player type
                session.setAttribute("selectedClass", player.getPlayerType().toUpperCase());
                
                // Ensure player state is saved before redirecting
                gameEngine.saveAllPlayersState();
                
                // Set persistent cookie for session recovery
                Cookie userIdCookie = new Cookie("user_id", String.valueOf(userId));
                userIdCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
                userIdCookie.setPath("/");
                resp.addCookie(userIdCookie);
                
                // Redirect to dashboard
                resp.sendRedirect(req.getContextPath() + "/dashboard");
            } else {
                // Handle player loading error
                req.setAttribute("errorMessage", "Failed to load player");
                req.getRequestDispatcher("/_view/Home.jsp").forward(req, resp);
            }
            
        } else if ("createNewPlayer".equals(action)) {
            // Create new player
            String playerName = req.getParameter("playerName");
            String playerClass = req.getParameter("playerClass");
            String playerDescription = req.getParameter("playerDescription");
            
            // Ensure the game engine is running
            if (!gameEngine.getRunning()) {
                gameEngine.startWithoutPlayer();
            }
            
            // Create player
            models.Player player = playerLoadManager.createNewPlayer(
                userId, 
                playerName, 
                playerClass, 
                playerDescription
            );
            
            if (player != null) {
                // Set initial currentRoomNum in player object
                player.setCurrentRoomNum(0); // Set to 0-indexed room number
                
                // Save to specified slot
                int slotNumber = Integer.parseInt(req.getParameter("slotNumber"));
                String saveName = "Slot " + slotNumber + ": " + playerName;
                
                // Default to starting room 1 (database uses 1-indexed rooms)
                playerLoadManager.savePlayerToSlot(
                    userId, 
                    player.getId(), 
                    1, // Starting room (1-indexed for database)
                    player.getHp(), 
                    slotNumber, 
                    saveName
                );
                
                // Add player to the GameEngine's players ArrayList
                int playerIndex = gameEngine.addPlayer(player);
                
                // Set player_id in session to the index in the players ArrayList
                session.setAttribute("player_id", playerIndex);
                
                // Set current room to 1 (0-indexed)
                gameEngine.setCurrentRoomNum(0);
                
                // Ensure player state is saved before redirecting
                gameEngine.saveAllPlayersState();
                
                // Store selected class in session
                session.setAttribute("selectedClass", playerClass.toUpperCase());
                
                // Redirect to dashboard
                resp.sendRedirect(req.getContextPath() + "/dashboard");
            } else {
                // Handle player creation error
                req.setAttribute("errorMessage", "Failed to create player");
                req.getRequestDispatcher("/_view/Home.jsp").forward(req, resp);
            }
        } else {
            // Invalid action
            resp.sendRedirect(req.getContextPath() + "/home");
        }
    }
}
