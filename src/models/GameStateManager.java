package models;

import java.sql.*;
import GameEngine.GameEngine;

public class GameStateManager {
    /** Call this at GameEngine.start() before loadData() */
    public static void loadState(GameEngine engine) {
        try (Connection conn = DerbyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(
               "SELECT current_room, player_hp, damage_multi, running_message, skill_points "
             + "FROM GAME_STATE WHERE state_id = 1");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                engine.setCurrentRoomNum(rs.getInt("current_room") - 1);
                engine.getPlayer().setHp(rs.getInt("player_hp"));
                engine.getPlayer().setdamageMulti(rs.getDouble("damage_multi"));
                engine.getPlayer().setSkillPoints(rs.getInt("skill_points"));
                
                // Load the running message
                String runningMessage = rs.getString("running_message");
                if (runningMessage != null) {
                    engine.setRunningMessage(runningMessage);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load game state", e);
        }
    }

    /** Call this after every turn or action that changes state */
    public static void saveState(GameEngine engine) {
        try (Connection conn = DerbyDatabase.getConnection()) {
            // Check if state exists
            boolean exists;
            try (PreparedStatement check = conn.prepareStatement(
                     "SELECT 1 FROM GAME_STATE WHERE state_id = 1"
                 )) {
                try (ResultSet rs = check.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                // Update existing state
                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE GAME_STATE SET current_room = ?, player_hp = ?, damage_multi = ?, running_message = ?, skill_points = ? WHERE state_id = 1")) {
                    
                    ps.setInt(1, engine.getCurrentRoomNum() + 1);  // 1-based room numbers
                    ps.setInt(2, engine.getPlayer().getHp());
                    ps.setDouble(3, engine.getPlayer().getdamageMulti());
                    ps.setString(4, engine.getRunningMessage());
                    ps.setInt(5, engine.getPlayer().getSkillPoints());
                    ps.executeUpdate();
                }
            } else {
                // Insert new state
                try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO GAME_STATE(state_id, current_room, player_hp, damage_multi, running_message, skill_points, last_saved) " +
                    "VALUES (1, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)")) {
                    
                    ps.setInt(1, engine.getCurrentRoomNum() + 1);
                    ps.setInt(2, engine.getPlayer().getHp());
                    ps.setDouble(3, engine.getPlayer().getdamageMulti());
                    ps.setString(4, engine.getRunningMessage());
                    ps.setInt(5, engine.getPlayer().getSkillPoints());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save game state", e);
        }
    }

}
