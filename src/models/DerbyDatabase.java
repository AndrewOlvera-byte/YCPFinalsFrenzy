package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
}
