
package models;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {
	
	
    /** Run on GameEngine.start(): create tables and seed CSVs if needed */
    public static void initialize() {
        try (Connection conn = DerpyDatabase.getConnection()) {
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
                seedTable(conn, "items.csv",           "INSERT INTO ITEM VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                seedTable(conn, "item_components.csv", "INSERT INTO ITEM_COMPONENT VALUES (?, ?)");
                seedTable(conn, "npcs.csv",            "INSERT INTO NPC VALUES (?, ?, ?, ?, ?, ?, ?)");
                seedTable(conn, "npc_room.csv",        "INSERT INTO NPC_ROOM VALUES (?, ?)");
                seedTable(conn, "npc_inventory.csv",   "INSERT INTO NPC_INVENTORY VALUES (?, ?)");
                seedTable(conn, "player.csv",          "INSERT INTO PLAYER VALUES (?, ?, ?, ?, ?, ?, ?)");
                seedTable(conn, "room_inventory.csv",  "INSERT INTO ROOM_INVENTORY VALUES (?, ?)");
                seedTable(conn, "player_inventory.csv","INSERT INTO PLAYER_INVENTORY VALUES (?, ?)");
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

    /** Execute each line in /db/schema.sql */
    private static void runDDL(Connection conn) throws Exception {
        try (InputStream in = DatabaseInitializer.class
                     .getResourceAsStream("/db/schema.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             Statement stmt = conn.createStatement()) {

            StringBuilder buf = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) continue;
                buf.append(line).append(" ");
                if (line.endsWith(";")) {
                    String sql = buf.toString();
                    // strip trailing semicolon
                    sql = sql.substring(0, sql.lastIndexOf(";")).trim();
                    try {
                        stmt.execute(sql);
                    } catch (SQLException sqle) {
                        String state = sqle.getSQLState();
                        // X0Y32 = “table/view already exists”; 42Y55 = same in newer versions
                        if ("X0Y32".equals(state) || "42Y55".equals(state)) {
                            // ignore
                        } else {
                            throw sqle;
                        }
                    }
                    buf.setLength(0);
                }
            }
        }
    }


    /** Load a CSV from /db/<csvFile> and run the given INSERT */
 // inside models/DatabaseInitializer.java

    /** 
     * Parse one CSV line into columns, handling quoted fields with commas.
     */
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // flip in/out of quoted mode
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                // end of field
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        // last field
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private static void seedTable(Connection conn, String csvFile, String insertSql) throws Exception {
        try (InputStream in = DatabaseInitializer.class
                     .getResourceAsStream("/db/" + csvFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] cols = parseCSVLine(line);      // ← use our CSV parser
                for (int i = 0; i < cols.length; i++) {
                    // Trim surrounding quotes, if any
                    String val = cols[i];
                    if (val.startsWith("\"") && val.endsWith("\"")) {
                        val = val.substring(1, val.length() - 1);
                    }
                    ps.setString(i + 1, val.isEmpty() ? null : val);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

}
