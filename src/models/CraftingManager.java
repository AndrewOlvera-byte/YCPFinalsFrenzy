package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import GameEngine.GameEngine;
import models.DerbyDatabase;
import models.Item;
import models.Utility;
import models.Weapon;
import models.Armor;
import models.ArmorSlot;

public class CraftingManager {
    private GameEngine engine;

    public CraftingManager(GameEngine engine) {
        this.engine = engine;
    }

    public String craftItem(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            return "\n<b>What do you want to craft?</b>";
        }
        String targetName = itemName.trim();
        try (Connection conn = DerbyDatabase.getConnection()) {
            // 1) Lookup target item_id case-insensitively
            int targetItemId = -1;
            String dbTargetName = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT item_id, name FROM ITEM WHERE UPPER(name) = UPPER(?)")) {
                ps.setString(1, targetName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        targetItemId = rs.getInt("item_id");
                        dbTargetName = rs.getString("name");
                    } else {
                        return "\n<b>You can't craft '" + targetName + "'.</b>";
                    }
                }
            }
            String displayName = (dbTargetName != null) ? dbTargetName : targetName;
            // 2) Gather components
            List<String> components = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT component FROM ITEM_COMPONENT WHERE item_id = ?")) {
                ps.setInt(1, targetItemId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        components.add(rs.getString("component"));
                    }
                }
            }
            if (components.isEmpty()) {
                return "\n<b>You can't craft '" + displayName + "'.</b>";
            }
            // 3) Verify player has each component
            for (String comp : components) {
                if (engine.CharItemNameToID(comp) < 0) {
                    return "\n<b>You don't have the necessary components to craft '"
                         + displayName + "'. Missing: " + comp + ".</b>";
                }
            }
            // 4) Perform crafting transaction
            conn.setAutoCommit(false);
            try {
                // Remove components
                for (String comp : components) {
                    int idx = engine.CharItemNameToID(comp);
                    engine.getPlayer().removeItem(idx);
                    // Find component ID in DB
                    int compItemId = -1;
                    try (PreparedStatement psGet = conn.prepareStatement(
                                "SELECT item_id FROM ITEM WHERE name = ?")) {
                        psGet.setString(1, comp);
                        try (ResultSet rs2 = psGet.executeQuery()) {
                            if (rs2.next()) {
                                compItemId = rs2.getInt("item_id");
                            }
                        }
                    }
                    if (compItemId < 0) {
                        throw new SQLException("Component not found in DB: " + comp);
                    }
                    try (PreparedStatement psDel = conn.prepareStatement(
                                "DELETE FROM PLAYER_INVENTORY WHERE player_id = 1 AND item_id = ?")) {
                        psDel.setInt(1, compItemId);
                        psDel.executeUpdate();
                    }
                }
                // Add the crafted item
                Item crafted = loadItem(targetItemId, conn);
                engine.getPlayer().addItem(crafted);
                try (PreparedStatement psIns = conn.prepareStatement(
                            "INSERT INTO PLAYER_INVENTORY (player_id, item_id) VALUES (1, ?)") ) {
                    psIns.setInt(1, targetItemId);
                    psIns.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            return "\n<b>You crafted a " + displayName + ".</b>";
        } catch (SQLException e) {
            throw new RuntimeException("Crafting failed", e);
        }
    }

    /** Disassemble an item into its components if disassemblable */
    public String disassembleItem(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            return "\n<b>What do you want to disassemble?</b>";
        }
        String targetName = itemName.trim();
        try (Connection conn = DerbyDatabase.getConnection()) {
            // 1) Lookup target item_id and disassemblable flag
            int targetItemId = -1;
            String dbTargetName = null;
            boolean disassemblable = false;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT item_id, name, disassemblable FROM ITEM WHERE UPPER(name) = UPPER(?)"
                )) {
                ps.setString(1, targetName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        targetItemId = rs.getInt("item_id");
                        dbTargetName = rs.getString("name");
                        disassemblable = rs.getBoolean("disassemblable");
                    } else {
                        return "\n<b>You can't disassemble '" + targetName + "'.</b>";
                    }
                }
            }
            String displayName = (dbTargetName != null) ? dbTargetName : targetName;
            if (!disassemblable) {
                return "\n<b>'" + displayName + "' cannot be disassembled.</b>";
            }
            // 2) Gather components
            List<String> components = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT component FROM ITEM_COMPONENT WHERE item_id = ?"
                )) {
                ps.setInt(1, targetItemId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        components.add(rs.getString("component"));
                    }
                }
            }
            if (components.isEmpty()) {
                return "\n<b>'" + displayName + "' cannot be disassembled.</b>";
            }
            // 3) Perform disassembly transaction
            conn.setAutoCommit(false);
            try {
                // Remove the item
                int idx = engine.CharItemNameToID(displayName);
                if (idx < 0) {
                    throw new SQLException("Item not in inventory: " + displayName);
                }
                engine.getPlayer().removeItem(idx);
                try (PreparedStatement psDel = conn.prepareStatement(
                        "DELETE FROM PLAYER_INVENTORY WHERE player_id = 1 AND item_id = ?"
                    )) {
                    psDel.setInt(1, targetItemId);
                    psDel.executeUpdate();
                }
                // Add components back
                for (String comp : components) {
                    int compId = -1;
                    String compDbName = null;
                    try (PreparedStatement psComp = conn.prepareStatement(
                            "SELECT item_id, name FROM ITEM WHERE UPPER(name) = UPPER(?)"
                        )) {
                        psComp.setString(1, comp);
                        try (ResultSet rs2 = psComp.executeQuery()) {
                            if (rs2.next()) {
                                compId = rs2.getInt("item_id");
                                compDbName = rs2.getString("name");
                            } else {
                                throw new SQLException("Component not found in DB: " + comp);
                            }
                        }
                    }
                    String compDisplayName = (compDbName != null) ? compDbName : comp;
                    Item componentItem = loadItem(compId, conn);
                    engine.getPlayer().addItem(componentItem);
                    try (PreparedStatement psIns = conn.prepareStatement(
                            "INSERT INTO PLAYER_INVENTORY (player_id, item_id) VALUES (1, ?)"
                        )) {
                        psIns.setInt(1, compId);
                        psIns.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            return "\n<b>'" + displayName + "' was disassembled into its components.</b>";
        } catch (SQLException e) {
            throw new RuntimeException("Disassemble failed", e);
        }
    }

    private Item loadItem(int itemId, Connection conn) throws SQLException {
        String sql = "SELECT name, value, weight, long_description, short_description,"
                   + " type, healing, damage_multi, attack_damage, attack_boost, defense_boost, slot"
                   + " FROM ITEM WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Item not found: " + itemId);
                }
                String name = rs.getString("name");
                int value = rs.getInt("value");
                int weight = rs.getInt("weight");
                String longDesc = rs.getString("long_description");
                String shortDesc = rs.getString("short_description");
                String type = rs.getString("type");
                switch (type.toUpperCase()) {
                case "UTILITY":
                    int healing = rs.getInt("healing");
                    double dmgMulti = rs.getDouble("damage_multi");
                    return new Utility(value, weight, name, null,
                            longDesc, shortDesc, healing, dmgMulti);
                case "WEAPON":
                    int attackDmg = rs.getInt("attack_damage");
                    return new Weapon(value, weight, name, null,
                            attackDmg, longDesc, shortDesc);
                case "ARMOR":
                    int healAmt = rs.getInt("healing");
                    double atkBoost = rs.getDouble("attack_boost");
                    int defBoost = rs.getInt("defense_boost");
                    ArmorSlot slot = ArmorSlot.valueOf(rs.getString("slot"));
                    return new Armor(value, weight, name, null,
                            longDesc, shortDesc, healAmt, atkBoost, defBoost, slot);
                default:
                    return new Item(value, weight, name, null,
                            longDesc, shortDesc);
                }
            }
        }
    }
} 