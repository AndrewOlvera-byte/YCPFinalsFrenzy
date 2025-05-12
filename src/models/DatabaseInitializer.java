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
                seedTable(conn, "rooms.csv",           "INSERT INTO ROOM VALUES (?, ?, ?, ?, ?)");
                seedTable(conn, "connections.csv",     "INSERT INTO ROOM_CONNECTIONS VALUES (?, ?, ?)");
                seedTable(conn, "items.csv",           "INSERT INTO ITEM VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                seedTable(conn, "item_components.csv", "INSERT INTO ITEM_COMPONENT VALUES (?, ?)");
                seedTable(conn, "npcs.csv",            "INSERT INTO NPC VALUES (?, ?, ?, ?, ?, ?, ?)");
                seedTable(conn, "npc_room.csv",        "INSERT INTO NPC_ROOM VALUES (?, ?)");
                seedTable(conn, "npc_inventory.csv",   "INSERT INTO NPC_INVENTORY VALUES (?, ?)");
                seedTable(conn, "player.csv",          "INSERT INTO PLAYER VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                seedTable(conn, "room_inventory.csv",  "INSERT INTO ROOM_INVENTORY VALUES (?, ?)");
                seedTable(conn, "player_inventory.csv","INSERT INTO PLAYER_INVENTORY VALUES (?, ?)");
                seedTable(conn, "conversation_nodes.csv","INSERT INTO CONVERSATION_NODES VALUES (?, ?, ?, ?, ?, ?, ?)");
                seedTable(conn, "conversation_edges.csv","INSERT INTO CONVERSATION_EDGES VALUES (?, ?, ?, ?)");
                seedTable(conn, "companion.csv",       "INSERT INTO COMPANION VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                seedTable(conn, "companion_room.csv",  "INSERT INTO COMPANION_ROOM VALUES (?, ?)");
                seedTable(conn, "player_companion.csv", "INSERT INTO PLAYER_COMPANION VALUES (?,?)");
                seedTable(conn, "companion_inventory.csv", "INSERT INTO COMPANION_INVENTORY VALUES (?, ?)");
                // Seed quest definitions from CSV
                seedQuestDefinitions(conn);
            }
        } catch (Exception e) {
            throw new RuntimeException("DB initialization failed", e);
        }
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
        InputStream in = DatabaseInitializer.class.getResourceAsStream("/db/" + csvFile);
        if (in == null) {
            return; // no data
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            // Read and parse header line to get column names
            String headerLine = reader.readLine();
            if (headerLine == null) {
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
                while ((line = reader.readLine()) != null) {
                    String[] cols = parseCSVLine(line);
                    if (cols.length < paramCount) {
                        throw new SQLException("Row has " + cols.length + " columns, expected >= " + paramCount + " in " + csvFile);
                    }
                    
                    for (int i = 0; i < paramCount; i++) {
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
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private static void seedQuestDefinitions(Connection conn) throws Exception {
        // Implementation of seedQuestDefinitions method
    }
}
