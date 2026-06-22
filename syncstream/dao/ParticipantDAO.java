package com.syncstream.dao;

import com.syncstream.db.DBConnection;

import java.sql.*;

public class ParticipantDAO {

    private final Connection conn;

    public ParticipantDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    public boolean joinRoom(int roomId, int userId, String role) {
        String sql = """
            INSERT INTO participants (room_id, user_id, role, joined_at, left_at)
            VALUES (?, ?, ?, NOW(), NULL)
            ON DUPLICATE KEY UPDATE joined_at = NOW(), left_at = NULL, role = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, userId);
            ps.setString(3, role);
            ps.setString(4, role);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ParticipantDAO] joinRoom error: " + e.getMessage());
        }
        return false;
    }

    public boolean leaveRoom(int roomId, int userId) {
        String sql = "UPDATE participants SET left_at = NOW() WHERE room_id = ? AND user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ParticipantDAO] leaveRoom error: " + e.getMessage());
        }
        return false;
    }

    public int getActiveCount(int roomId) {
        String sql = "SELECT COUNT(*) FROM participants WHERE room_id = ? AND left_at IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ParticipantDAO] getActiveCount error: " + e.getMessage());
        }
        return 0;
    }
}