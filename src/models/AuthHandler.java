package models;

import java.sql.*;

public class AuthHandler {
    // Singleton
    private static AuthHandler instance;
    public AuthHandler() { }
    public static synchronized AuthHandler getInstance() {
        if (instance == null) instance = new AuthHandler();
        return instance;
    }

    /**
     * Returns true if there is a user with this usernameOrEmail and password.
     */
    public boolean validateUser(String usernameOrEmail, String password) {
        String sql = "SELECT 1 FROM users WHERE (username = ? OR email = ?) AND password = ?";
        try (Connection conn = DerbyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);
            ps.setString(3, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error validating user", ex);
        }
    }

    /**
     * Returns the user_id for this usernameOrEmail, or -1 if none.
     */
    public int getUserID(String usernameOrEmail) {
        String sql = "SELECT user_id FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DerbyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("user_id") : -1;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching user ID", ex);
        }
    }

    /**
     * Attempts to insert a new user.
     * Returns true on success, false if username/email conflict.
     */
    public boolean createUser(String username, String email, String password) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DerbyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            // Derby unique‐constraint SQLState is X0Y32
            if ("X0Y32".equals(ex.getSQLState())) return false;
            throw new RuntimeException("Error creating user", ex);
        }
    }

    /** 
     * Returns true if a user already has this username. 
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DerbyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error checking username", ex);
        }
    }

    /** 
     * Returns true if a user already has this email. 
     */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DerbyDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error checking email", ex);
        }
    }
}
