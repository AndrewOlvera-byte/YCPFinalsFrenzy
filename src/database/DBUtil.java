package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for database operations.
 */
public class DBUtil {
    /**
     * Closes a Connection without throwing any exceptions.
     * 
     * @param conn The Connection to close
     */
    public static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Closes a Statement without throwing any exceptions.
     * 
     * @param stmt The Statement to close
     */
    public static void closeQuietly(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Closes a ResultSet without throwing any exceptions.
     * 
     * @param resultSet The ResultSet to close
     */
    public static void closeQuietly(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }
} 