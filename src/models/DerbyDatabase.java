package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DerbyDatabase {
    private static final String URL = "jdbc:derby:./derpydb;create=true";

    static {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Derby driver not found", e);
        }
    }

    /** Get a live connection to DerpyDatabase */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void reset(String[] tableNames) {
    	try (Connection conn = DerbyDatabase.getConnection()) {
    		// Create a statement object to execute the queries
    		try (Statement stmt = conn.createStatement()) {
    			for (String tableName : tableNames) {
    				// Check if the table has any rows by using a SELECT COUNT query
    				try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
    					if (rs.next()) {
    						int rowCount = rs.getInt(1);
                       
    						// Only drop the table if there are rows in it
    						if (rowCount > 0) {
    							stmt.executeUpdate("DELETE FROM " + tableName);
    						}
    					}
    				}
    			}
    		}
    	} catch (SQLException e) {
    		throw new RuntimeException("Error clearing tables", e);
    	}
	}
}

