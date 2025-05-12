package models;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {

    /** Run on GameEngine.start(): create tables and seed CSVs if needed */
    public static void initialize() {
        try (Connection conn = DerbyDatabase.getConnection()) {
            boolean alreadySeeded = false;

            // 1) Check if ROOM exists and has rows
            try {
                alreadySeeded = isSeeded(conn);
            } catch (SQLException sqle) {
                // SQL state 42X05 = table/view not found
                if ("42X05".equals(sqle.getSQLState())) {
                    alreadySeeded = false;
                } else {
                    throw sqle;
                }
            }

            // 2) If not seeded, run DDL and seed all tables
            if (!alreadySeeded) {
                System.out.println("Seeding DerpyDatabase for the first time...");
                runDDL(conn);
                
                // Load items FIRST to satisfy foreign key constraints
                seedTable(conn, "items.csv",           "INSERT INTO ITEM VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                seedTable(conn, "item_components.csv", "INSERT INTO ITEM_COMPONENT VALUES (?, ?)");
                
                // Seed quest definitions if there's a quests.csv file
                try {
                    seedTable(conn, "quests.csv", "INSERT INTO quest_definition(quest_id, name, description, target_type, target_name, target_count, reward_skill_points, trigger_type, trigger_target) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    System.out.println("Quests data seeded from quests.csv");
                } catch (Exception e) {
                    System.out.println("Warning: Could not seed quests data: " + e.getMessage());
                }
                
                // Then seed room-related tables
                seedRoomsFromCSV(conn);
                
                // Load NPC and conversation data (from main branch)
                seedTable(conn, "npcs.csv",            "INSERT INTO NPC VALUES (?, ?, ?, ?, ?, ?, ?)");
                seedTable(conn, "npc_room.csv",        "INSERT INTO NPC_ROOM VALUES (?, ?)");
                seedTable(conn, "npc_inventory.csv",   "INSERT INTO NPC_INVENTORY VALUES (?, ?)");
                
                // These tables were added in main branch for player data
                // For MMO functionality, we'll keep them optional
                try {
                    seedTable(conn, "player.csv",          "INSERT INTO PLAYER VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    seedTable(conn, "room_inventory.csv",  "INSERT INTO ROOM_INVENTORY VALUES (?, ?)");
                    seedTable(conn, "player_inventory.csv","INSERT INTO PLAYER_INVENTORY VALUES (?, ?)");
                    System.out.println("Initial player data seeded from CSVs");
                } catch (Exception e) {
                    System.out.println("Note: Player tables will start empty for MMO functionality: " + e.getMessage());
                }
                
                seedTable(conn, "conversation_nodes.csv","INSERT INTO CONVERSATION_NODES VALUES (?, ?, ?, ?, ?, ?, ?)");
                seedTable(conn, "conversation_edges.csv","INSERT INTO CONVERSATION_EDGES VALUES (?, ?, ?, ?)");
                seedTable(conn, "companion.csv",       "INSERT INTO COMPANION VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                seedTable(conn, "companion_room.csv",  "INSERT INTO COMPANION_ROOM VALUES (?, ?)");
                seedTable(conn, "companion_inventory.csv", "INSERT INTO COMPANION_INVENTORY VALUES (?, ?)");
                
                // Database initialization complete
                System.out.println("Database initialization complete");
            }
        } catch (Exception e) {
            throw new RuntimeException("DB initialization failed", e);
        }
    }
    
    /**
     * Seeds room-related tables from CSV files
     * This is used both for initial database creation and for room reinitialization
     * @param conn An active database connection
     * @throws Exception If seeding fails
     */
    public static void seedRoomsFromCSV(Connection conn) throws Exception {
        System.out.println("Seeding entity tables first (ROOM, NPC, COMPANION)...");
        
        // First seed the entity tables (need to exist before relationships)
        seedTable(conn, "rooms.csv",                "INSERT INTO ROOM VALUES (?, ?, ?, ?, ?)");
        seedTable(conn, "npcs.csv",                 "INSERT INTO NPC VALUES (?, ?, ?, ?, ?, ?, ?)");
        seedTable(conn, "companion.csv",            "INSERT INTO COMPANION VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        seedTable(conn, "conversation_nodes.csv",   "INSERT INTO CONVERSATION_NODES VALUES (?, ?, ?, ?, ?, ?, ?)");
        
        System.out.println("Seeding relationship tables (connections, inventories, placements)...");
        
        // Then seed relationships between entities
        seedTable(conn, "connections.csv",          "INSERT INTO ROOM_CONNECTIONS VALUES (?, ?, ?)");
        seedTable(conn, "conversation_edges.csv",   "INSERT INTO CONVERSATION_EDGES VALUES (?, ?, ?, ?)");
        seedTable(conn, "npc_room.csv",             "INSERT INTO NPC_ROOM VALUES (?, ?)");
        seedTable(conn, "companion_room.csv",       "INSERT INTO COMPANION_ROOM VALUES (?, ?)");
        seedTable(conn, "room_inventory.csv",       "INSERT INTO ROOM_INVENTORY VALUES (?, ?)");
        seedTable(conn, "npc_inventory.csv",        "INSERT INTO NPC_INVENTORY VALUES (?, ?)");
        
        System.out.println("Room data seeded successfully");
    }

    /** Returns true if ROOM exists *and* has at least one row */
    private static boolean isSeeded(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ROOM")) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    /** Execute all statements in /db/schema.sql, splitting on ';' */
    private static void runDDL(Connection conn) throws Exception {
        // Load entire file
        InputStream in = DatabaseInitializer.class.getResourceAsStream("/db/schema.sql");
        if (in == null) {
            throw new RuntimeException("Could not find /db/schema.sql on classpath");
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }
                sb.append(line).append("\n");
            }
        }

        // Split and execute each statement
        String[] statements = sb.toString().split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String raw : statements) {
                String sql = raw.trim();
                if (sql.isEmpty()) {
                    continue;
                }
                try {
                    stmt.execute(sql);
                } catch (SQLException sqle) {
                    String state = sqle.getSQLState();
                    // ignore "table/view already exists"
                    if (!"X0Y32".equals(state) && !"42Y55".equals(state)) {
                        throw sqle;
                    }
                }
            }
        }
    }

    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private static void seedTable(Connection conn, String csvFile, String insertSql) throws Exception {
        System.out.println("Seeding from " + csvFile + "...");
        
        InputStream in = DatabaseInitializer.class.getResourceAsStream("/db/" + csvFile);
        if (in == null) {
            System.out.println("Warning: Could not find CSV file " + csvFile);
            return; // no data
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            // Read and parse header line to get column names
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("Warning: CSV file " + csvFile + " is empty (has only a header)");
                return; // no data
            }
            String[] headers = parseCSVLine(headerLine);

            // Count the number of parameters in the SQL statement
            int paramCount = 0;
            for (int i = 0; i < insertSql.length(); i++) {
                if (insertSql.charAt(i) == '?') {
                    paramCount++;
                }
            }

            // Ensure CSV has at least as many columns as parameters
            if (headers.length < paramCount) {
                throw new SQLException("CSV columns (" + headers.length + ") < SQL parameters (" + paramCount + ") for " + csvFile);
            }

            // Prepare statement
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                String line;
                int rowCount = 0;
                int insertedCount = 0;
                
                // Enable batch mode
                conn.setAutoCommit(false);
                
                while ((line = reader.readLine()) != null) {
                    rowCount++;
                    String[] cols = parseCSVLine(line);
                    if (cols.length != headers.length) {
                        System.out.println("Warning: Row " + rowCount + " has " + cols.length + 
                                " columns, expected " + headers.length + " in " + csvFile + ". Skipping row.");
                        continue;
                    }
                    
                    try {
                        for (int i = 0; i < cols.length; i++) {
                            String header = headers[i].trim();
                            String val = cols[i].trim();
                            // Unwrap quoted values
                            if (val.startsWith("\"") && val.endsWith("\"")) {
                                val = val.substring(1, val.length() - 1);
                            }
                            if (val.isEmpty()) {
                                ps.setNull(i + 1, Types.VARCHAR);
                            }
                            // Boolean fields (underscore or camelCase)
                            else if (header.equalsIgnoreCase("is_root")) {
                                ps.setBoolean(i + 1, Boolean.parseBoolean(val));
                            } else if (header.equalsIgnoreCase("become_aggressive")
                                    || header.equalsIgnoreCase("becomeAggressive")) {
                                ps.setBoolean(i + 1, Boolean.parseBoolean(val));
                            } else if (header.equalsIgnoreCase("drop_item")
                                    || header.equalsIgnoreCase("dropItem")) {
                                ps.setBoolean(i + 1, Boolean.parseBoolean(val));
                            }
                            // Integer field (underscore or camelCase)
                            else if (header.equalsIgnoreCase("item_to_drop")
                                    || header.equalsIgnoreCase("itemToDrop")) {
                                ps.setInt(i + 1, Integer.parseInt(val));
                            }
                            // Default string binding
                            else {
                                ps.setString(i + 1, val);
                            }
                        }
                        
                        // Execute individual inserts instead of batching to better handle errors
                        ps.executeUpdate();
                        insertedCount++;
                        
                    } catch (SQLException e) {
                        // Check for duplicate key violation
                        if (e.getSQLState() != null && (e.getSQLState().equals("23505") || 
                            e.getMessage().contains("duplicate key value") || 
                            e.getMessage().contains("unique constraint"))) {
                            
                            System.out.println("Warning: Row " + rowCount + " in " + csvFile + 
                                " has duplicate key. Row skipped.");
                                
                            // Roll back just this transaction
                            conn.rollback();
                        } else {
                            // Some other SQL error, propagate it
                            throw e;
                        }
                    }
                }
                
                // Commit all successful inserts
                conn.commit();
                
                System.out.println("Inserted " + insertedCount + " out of " + rowCount + 
                    " rows from " + csvFile);
            } catch (Exception e) {
                // Roll back on any error
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
                throw e;
            } finally {
                // Restore auto-commit
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
        }
    }

    private static void seedQuestDefinitions(Connection conn) throws Exception {
        // Implementation of seedQuestDefinitions method
    }
}
