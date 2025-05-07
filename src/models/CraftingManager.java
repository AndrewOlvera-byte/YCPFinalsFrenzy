package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

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

    /** Combine exactly two components by matching them to a recipe */
    public String combineItems(String compA, String compB) {
        if (compA == null || compA.trim().isEmpty() || compB == null || compB.trim().isEmpty()) {
            return "\n<b>You must specify two components to combine.</b>";
        }
        String a = compA.trim();
        String b = compB.trim();
        Set<String> provided = new HashSet<>(Arrays.asList(a, b));
        try (Connection conn = DerbyDatabase.getConnection()) {
            // Load all recipes
            Map<Integer, Set<String>> recipes = new HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT item_id, component FROM ITEM_COMPONENT");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("item_id");
                    recipes.computeIfAbsent(id, k -> new HashSet<>()).add(rs.getString("component"));
                }
            }
            // Find recipe matching exactly provided components
            int targetId = -1;
            for (Map.Entry<Integer, Set<String>> e : recipes.entrySet()) {
                Set<String> comps = e.getValue();
                if (comps.size() == provided.size() && comps.containsAll(provided)) {
                    if (targetId != -1) {
                        return "\n<b>Multiple recipes match those components.</b>";
                    }
                    targetId = e.getKey();
                }
            }
            if (targetId < 0) {
                return "\n<b>No recipe for combining those components.</b>";
            }
            // Lookup crafted item name
            String targetName = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT name FROM ITEM WHERE item_id = ?")) {
                ps.setInt(1, targetId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        targetName = rs.getString("name");
                    }
                }
            }
            if (targetName == null) {
                return "\n<b>Crafted item not found in DB.</b>";
            }
            // Perform the transaction
            conn.setAutoCommit(false);
            try {
                // Remove components
                for (String comp : provided) {
                    int idx = engine.CharItemNameToID(comp);
                    if (idx < 0) throw new SQLException("Missing component: " + comp);
                    engine.getPlayer().removeItem(idx);
                    int compId = -1;
                    try (PreparedStatement ps2 = conn.prepareStatement(
                            "SELECT item_id FROM ITEM WHERE UPPER(name) = UPPER(?)")) {
                        ps2.setString(1, comp);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            if (rs2.next()) compId = rs2.getInt("item_id");
                        }
                    }
                    try (PreparedStatement del = conn.prepareStatement(
                            "DELETE FROM PLAYER_INVENTORY WHERE player_id = 1 AND item_id =?")) {
                        del.setInt(1, compId);
                        del.executeUpdate();
                    }
                }
                // Add crafted item
                Item crafted = loadItem(targetId, conn);
                engine.getPlayer().addItem(crafted);
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO PLAYER_INVENTORY(player_id, item_id) VALUES (1, ?)")) {
                    ins.setInt(1, targetId);
                    ins.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            return "\n<b>You combined " + a + " and " + b + " into " + targetName + ".</b>";
        } catch (SQLException e) {
            throw new RuntimeException("Combine failed", e);
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