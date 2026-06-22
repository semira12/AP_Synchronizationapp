package com.syncstream.dao;

import com.syncstream.db.DBConnection;
import com.syncstream.model.User;
import com.syncstream.util.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private final Connection conn;

    public UserDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }


    // CREATE — Register a new user

    public int registerUser(String username, String email, String plainPassword) {
        String hashedPassword = PasswordUtil.hash(plainPassword);  // SHA-256 hash

        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        // The ? marks are placeholders — they prevent SQL injection
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);

            int affected = ps.executeUpdate();   // runs the INSERT
            if (affected > 0) {
                // MySQL gives us the auto-generated primary key
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    System.out.println("[UserDAO] Registered user: " + username + " (id=" + newId + ")");
                    return newId;
                }
            }
        } catch (SQLException e) {
            // Duplicate username or email will throw a SQL exception
            System.err.println("[UserDAO] Register failed: " + e.getMessage());
        }
        return -1;  // registration failed
    }


    // READ — Login / authenticate

    /**
     * Checks username + password and returns the User if valid.
     *
     * @return User object if credentials match, null otherwise
     */
    public User login(String username, String plainPassword) {
        String hashedPassword = PasswordUtil.hash(plainPassword);
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashedPassword);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = mapRowToUser(rs);
                updateLastLogin(user.getUserId());
                System.out.println("[UserDAO] Login success: " + username);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Login error: " + e.getMessage());
        }
        System.out.println("[UserDAO] Login failed for: " + username);
        return null;  // bad credentials
    }


    // READ — Get user by ID

    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getUserById error: " + e.getMessage());
        }
        return null;
    }

    // READ — Check if username already exists
    public boolean usernameExists(String username) {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();   // true if any row was returned
        } catch (SQLException e) {
            System.err.println("[UserDAO] usernameExists error: " + e.getMessage());
        }
        return false;
    }

    // READ — Get all users (for admin / analytics)

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllUsers error: " + e.getMessage());
        }
        return users;
    }

    // UPDATE — Record last login timestamp
    private void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserDAO] updateLastLogin error: " + e.getMessage());
        }
    }


    // DELETE — Soft-delete (just mark inactive)

    public boolean deactivateUser(int userId) {
        String sql = "UPDATE users SET is_active = 0 WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] deactivateUser error: " + e.getMessage());
        }
        return false;
    }


    // HELPER — Map a ResultSet row to a User object

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setAvatarUrl(rs.getString("avatar_url"));
        u.setActive(rs.getBoolean("is_active"));

        // Timestamps can be null — handle carefully
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) u.setCreatedAt(created.toLocalDateTime());

        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) u.setLastLogin(lastLogin.toLocalDateTime());

        return u;
    }
}
