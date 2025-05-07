package models;

import java.sql.*;
import java.util.*;
import GameEngine.GameEngine;
import models.DerbyDatabase;

public class PlayerManager {
    private GameEngine engine;

    public PlayerManager(GameEngine engine) {
        this.engine = engine;
    }

    public void loadPlayer() {
        try (Connection conn = DerbyDatabase.getConnection()) {
            // 1) load main record (we assume ID=1)
            String sql = "SELECT name, hp, skill_points, damage_multi, long_description, short_description,player_type,attack_boost,defense_boost "
                       + "FROM PLAYER WHERE player_id = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No PLAYER with ID=1");
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
                         "SELECT item_id FROM PLAYER_INVENTORY WHERE player_id = 1"
                     );
                     ResultSet irs = ips.executeQuery()) {
                    while (irs.next()) {
                        inv.addItem(loadItem(conn, irs.getInt("item_id")));
                    }
                }
                Player player;

                switch (Class) {
                    case "ATTACK":
                        player = new Player(name, hp, sp, inv, ldesc, sdesc, dm, 20, 0);
                        break;
                    case "DEFENSE":
                        player = new Player(name, hp, sp, inv, ldesc, sdesc, dm, 0, 20);
                        break;
                    case "NORMAL":
                    default:
                        player = new Player(name, hp, sp, inv, ldesc, sdesc, dm, 0, 0);
                        break;
                }

                loadEquippedArmor(conn, player);  // 🔁 Load from DB
                engine.setPlayer(player);         // ✅ Set player after fully loaded

                
            }
                
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load player", ex);
        }
    }
    /** Duplicate the same helper from RoomManager */
    private Item loadItem(Connection conn, int itemId) throws SQLException {
        String sql =
            "SELECT name, value, weight,\n" +
            "       long_description, short_description,\n" +
            "       type, healing, damage_multi, attack_damage,\n" +
            "       attack_boost, defense_boost, slot\n" +
            "  FROM ITEM\n" +
            " WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("ITEM not found: " + itemId);
                }

                String name      = rs.getString("name");
                int    value     = rs.getInt("value");
                int    weight    = rs.getInt("weight");
                String longDesc  = rs.getString("long_description");
                String shortDesc = rs.getString("short_description");
                String type      = rs.getString("type");

                switch (type.toUpperCase()) {
                    case "UTILITY":
                        int    healing  = rs.getInt("healing");
                        double dmgMulti= rs.getDouble("damage_multi");
                        return new Utility(
                            value, weight, name, null,
                            longDesc, shortDesc,
                            healing, dmgMulti
                        );

                    case "WEAPON":
                        int attackDmg = rs.getInt("attack_damage");
                        return new Weapon(
                            value, weight, name, null,
                            attackDmg,
                            longDesc, shortDesc
                        );

                    case "ARMOR":
                        // Now these columns actually exist in your ResultSet:
                        int   healAmt     = rs.getInt("healing");
                        double atkBoost  = rs.getDouble("attack_boost");
                        int   defBoost   = rs.getInt("defense_boost");
                        ArmorSlot slot    = ArmorSlot.valueOf(rs.getString("slot"));
                        return new Armor(
                            value, weight, name, null,
                            longDesc, shortDesc,
                            healAmt,
                            atkBoost,
                            defBoost,
                            slot
                        );

                    default:
                        return new Item(
                            value, weight, name, null,
                            longDesc, shortDesc
                        );
                }
            }
        }
    }
    
    private void loadEquippedArmor(Connection conn, Player player) throws SQLException {
        String sql = "SELECT pe.slot, i.item_id FROM PLAYER_EQUIPMENT pe " +
                     "JOIN ITEM i ON pe.armor_id = i.item_id " +
                     "WHERE pe.player_id = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ArmorSlot slot = ArmorSlot.valueOf(rs.getString("slot"));
                int itemId = rs.getInt("item_id");
                Item item = loadItem(conn, itemId);
                if (item instanceof Armor) {
                    player.equip(slot, (Armor) item);
                } else {
                    System.err.println("Warning: Equipped item is not armor: " + item.getName());
                }
            }
        }
    }

    /**
     * Load a player by class (ATTACK, DEFENSE, NORMAL) from the DB.
     */
    public void loadPlayerByType(String cls) throws SQLException {
        String sql = "SELECT name, hp, skill_points, damage_multi,"
                   + " long_description, short_description,"
                   + " attack_boost, defense_boost"
                   + " FROM PLAYER WHERE player_type = ?";
        try (Connection conn = DerbyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cls);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No PLAYER of class=" + cls);
                }
                // build Player exactly as in loadPlayer()
                Player p = new Player(
                    rs.getString("name"),
                    rs.getInt("hp"),
                    rs.getInt("skill_points"),
                    new Inventory(new ArrayList<>(), 100),  // or load inventory if desired
                    rs.getString("long_description"),
                    rs.getString("short_description"),
                    rs.getDouble("damage_multi"),
                    rs.getInt("attack_boost"),
                    rs.getInt("defense_boost")
                );
                engine.setPlayer(p);
            }
        }
    }
}
    



		
