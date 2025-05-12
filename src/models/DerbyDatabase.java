package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DerbyDatabase {
    private static final String URL = "jdbc:derby:./derpydb;create=true";
    private static boolean isShutdown = false;

    static {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Derby driver not found", e);
        }
    }

    /** Get a live connection to DerpyDatabase */
    public static Connection getConnection() throws SQLException {
        if (isShutdown) {
            throw new SQLException("Database has been shut down");
        }
        return DriverManager.getConnection(URL);
    }

    /** Properly shutdown the database */
    public static void shutdown() {
        if (!isShutdown) {
            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } catch (SQLException e) {
                // SQLException is expected on shutdown
                if (!e.getSQLState().equals("XJ015")) {
                    e.printStackTrace();
                }
            }
            isShutdown = true;
        }
    }

    public static void reset(String[] tableNames) {
        try (Connection conn = DerbyDatabase.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                for (String tableName : tableNames) {
                    try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            stmt.executeUpdate("DELETE FROM " + tableName);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing tables", e);
        }
    }
}

