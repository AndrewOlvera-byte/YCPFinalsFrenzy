package models;

import java.sql.*;
import java.util.*;
import GameEngine.GameEngine;
import models.DerpyDatabase;

public class PlayerManager {
    private GameEngine engine;

    public PlayerManager(GameEngine engine) {
        this.engine = engine;
    }

    public void loadPlayer() {
        try (Connection conn = DerpyDatabase.getConnection()) {
            // 1) load main record (we assume ID=1)
            String sql = "SELECT name, hp, skill_points, damage_multi, long_description, short_description,player_type,attack_boost,defense_boost "
                       + "FROM PLAYER WHERE player_id = 2";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No PLAYER with ID=2");
                }
                String name  = rs.getString("name");
                int    hp    = rs.getInt("hp");
                int    sp    = rs.getInt("skill_points");
                double dm    = rs.getDouble("damage_multi");
                String ldesc = rs.getString("long_description");
                String sdesc = rs.getString("short_description");
                String Class = rs.getString("player_type");
                
                
                // 2) load inventory
                Inventory inv = new Inventory(new ArrayList<>(), 30);
                try (PreparedStatement ips = conn.prepareStatement(
                         "SELECT item_id FROM PLAYER_INVENTORY WHERE player_id = 2"
                     );
                     ResultSet irs = ips.executeQuery()) {
                    while (irs.next()) {
                        inv.addItem(loadItem(conn, irs.getInt("item_id")));
                    }
                }
                switch (Class) {
                case "ATTACK":
                    double attackBoost = rs.getDouble("attack_boost");
                    engine.setPlayer(
                      new AttackPlayer(name, hp, sp, inv, ldesc, sdesc, 1, attackBoost)
                    );
                    break;

                case "DEFENSE":  // <— correct spelling
                    double defenseBoost = rs.getDouble("defense_boost");
                    engine.setPlayer(
                      new DefensePlayer(name, hp, sp, inv, ldesc, sdesc, 1, defenseBoost)
                    );
                    break;

                case "NORMAL":
                default:
                    engine.setPlayer(
                      new Player(name, hp, sp, inv, ldesc, sdesc, dm)
                    );
                    break;
              }
                
                
            }
                
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load player", ex);
        }
    }
    /** Duplicate the same helper from RoomManager */
    private Item loadItem(Connection conn, int itemId) throws SQLException {
        String sql =
            "SELECT name, value, weight, long_description, short_description, " +
            "       type, healing, damage_multi, attack_damage " +
            "  FROM ITEM " +
            " WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("ITEM not found: " + itemId);
                }

                String name        = rs.getString("name");
                int    value       = rs.getInt("value");
                int    weight      = rs.getInt("weight");
                String longDesc    = rs.getString("long_description");
                String shortDesc   = rs.getString("short_description");
                String type        = rs.getString("type");

                switch (type.toUpperCase()) {
                    case "UTILITY":
                        int    healing     = rs.getInt("healing");
                        double dmgMulti    = rs.getDouble("damage_multi");
                        return new Utility(
                            value, weight, name,
                            /*components=*/null,
                            longDesc, shortDesc,
                            healing, dmgMulti
                        );

                    case "WEAPON":
                        int attackDmg      = rs.getInt("attack_damage");
                        return new Weapon(
                            value, weight, name,
                            /*components=*/null,
                            attackDmg,
                            longDesc, shortDesc
                        );

                    default:
                        // fallback to plain Item
                        return new Item(
                            value, weight, name,
                            /*components=*/null,
                            longDesc, shortDesc
                        );
                }
            }
        }
    }


}
