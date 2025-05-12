package models;

import java.sql.*;

/**
 * Handles database schema migrations to support the new persistence model
 * that separates player data (persistent) from room data (refreshed on restart).
 */
public class DatabaseMigration {
    
    /**
     * Check if the database needs migration to the new schema
     * @return true if migration is needed, false otherwise
     */
    public static boolean needsMigration() {
        try (Connection conn = DerbyDatabase.getConnection()) {
            // Check if player_items table exists (new schema)
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "PLAYER_ITEMS", null)) {
                if (rs.next()) {
                    // Table already exists, no migration needed
                    return false;
                }
            }
            
            // Check if PLAYER_INVENTORY table exists (old schema)
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "PLAYER_INVENTORY", null)) {
                if (rs.next()) {
                    // Old table exists but not new one, migration needed
                    return true;
                }
            }
            
            // Neither table exists, this might be a new database
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking if migration is needed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Migrate the database to the new schema with separated player and room data
     * @return true if successful, false otherwise
     */
    public static boolean migrateToNewSchema() {
        try (Connection conn = DerbyDatabase.getConnection()) {
            if (!needsMigration()) {
                System.out.println("No migration needed - database already on new schema");
                return true;
            }
            
            System.out.println("Starting database migration to new persistence schema...");
            
            // 1. Create new player_items table
            String createPlayerItemsTable = 
                "CREATE TABLE player_items (" +
                "  player_id  INT, " +
                "  item_id    INT, " +
                "  PRIMARY KEY (player_id, item_id), " +
                "  FOREIGN KEY (player_id)  REFERENCES PLAYER(player_id), " +
                "  FOREIGN KEY (item_id)    REFERENCES ITEM(item_id) " +
                ")";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(createPlayerItemsTable);
                System.out.println("Created player_items table");
            } catch (SQLException e) {
                if ("X0Y32".equals(e.getSQLState())) {
                    // Table already exists, which is fine
                    System.out.println("player_items table already exists");
                } else {
                    throw e;
                }
            }
            
            // 2. Copy data from PLAYER_INVENTORY to player_items
            try (Statement stmt = conn.createStatement()) {
                int rowsCopied = stmt.executeUpdate(
                    "INSERT INTO player_items " +
                    "SELECT player_id, item_id FROM PLAYER_INVENTORY " +
                    "WHERE NOT EXISTS (" +
                    "  SELECT 1 FROM player_items pi " +
                    "  WHERE pi.player_id = PLAYER_INVENTORY.player_id " +
                    "  AND pi.item_id = PLAYER_INVENTORY.item_id" +
                    ")"
                );
                System.out.println("Copied " + rowsCopied + " rows from PLAYER_INVENTORY to player_items");
            } catch (SQLException e) {
                System.err.println("Warning: Could not copy data from PLAYER_INVENTORY: " + e.getMessage());
                // Continue with migration even if this fails
            }
            
            // 3. We'll keep the PLAYER_INVENTORY table for backward compatibility
            // but update code to use player_items going forward
            
            System.out.println("Database migration completed successfully");
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to migrate database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 