package models;

import java.sql.*;
import GameEngine.GameEngine;

public class GameStateManager {
    /** Call this at GameEngine.start() before loadData() */
    public static void loadState(GameEngine engine) {
        try (Connection conn = DerpyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(
               "SELECT current_room, player_hp, damage_multi "
             + "FROM GAME_STATE WHERE state_id = 1");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                engine.setCurrentRoomNum(rs.getInt("current_room") - 1);
                engine.getPlayer().setHp(rs.getInt("player_hp"));
             
                engine.getPlayer().setdamageMulti(rs.getDouble("damage_multi"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load game state", e);
        }
    }

    /** Call this after every turn or action that changes state */
    public static void saveState(GameEngine engine) {
        try (Connection conn = DerpyDatabase.getConnection()) {
            // 1) Check if state row already exists
            boolean exists;
            try (PreparedStatement check = conn.prepareStatement(
                     "SELECT 1 FROM GAME_STATE WHERE state_id = 1"
                 )) {
                try (ResultSet rs = check.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                // 2a) UPDATE the existing row
                String updateSql =
                    "UPDATE GAME_STATE " +
                    "   SET current_room = ?, " +
                    "       player_hp    = ?, " +
                    "       damage_multi = ?, " +
                    "       last_saved   = CURRENT_TIMESTAMP " +
                    " WHERE state_id     = 1";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt   (1, engine.getCurrentRoomNum() + 1);
                    ps.setInt   (2, engine.getPlayer().getHp());
                    ps.setDouble(3, engine.getPlayer().getdamageMulti());
                    ps.executeUpdate();
                }
            } else {
                // 2b) INSERT a brand-new row
                String insertSql =
                    "INSERT INTO GAME_STATE(" +
                    "    state_id, current_room, player_hp, damage_multi, last_saved" +
                    ") VALUES (" +
                    "    1, ?, ?, ?, CURRENT_TIMESTAMP" +
                    ")";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt   (1, engine.getCurrentRoomNum() + 1);
                    ps.setInt   (2, engine.getPlayer().getHp());
                    ps.setDouble(3, engine.getPlayer().getdamageMulti());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save game state", e);
        }
    }

}
