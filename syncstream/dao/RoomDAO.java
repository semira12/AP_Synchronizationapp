package com.syncstream.dao;

import com.syncstream.db.DBConnection;
import com.syncstream.model.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RoomDAO {

    private final Connection conn;

    public RoomDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }


    // CREATE — Host creates a new room

    /**
     * Creates a new room and returns its generated room_id.
     * A random 6-character room code is generated automatically.
     *
     * @param roomName    display name for the room
     * @param hostUserId  user_id of the person creating the room
     * @param videoPath   path to the video file (local path or URL)
     * @return new room_id, or -1 if failed
     */
    public int createRoom(String roomName, int hostUserId, String videoPath) {
        String roomCode = generateRoomCode();   // e.g. "A3F9KL"
        String sql = "INSERT INTO rooms (room_code, room_name, host_user_id, video_path) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, roomCode);
            ps.setString(2, roomName);
            ps.setInt(3, hostUserId);
            ps.setString(4, videoPath);

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int newId = keys.getInt(1);
                System.out.println("[RoomDAO] Room created: " + roomName + " (code=" + roomCode + ")");
                return newId;
            }
        } catch (SQLException e) {
            System.err.println("[RoomDAO] createRoom error: " + e.getMessage());
        }
        return -1;
    }


    // READ — Find a room by its join code

    /**
     * Used when a viewer enters the short room code (e.g. "A3F9KL")
     * to join a room.
     */
    public Room getRoomByCode(String roomCode) {
        // JOIN with users table to also get the host's username
        String sql = """
            SELECT r.*, u.username AS host_username
            FROM rooms r
            JOIN users u ON r.host_user_id = u.user_id
            WHERE r.room_code = ? AND r.is_active = 1
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRowToRoom(rs);
        } catch (SQLException e) {
            System.err.println("[RoomDAO] getRoomByCode error: " + e.getMessage());
        }
        return null;
    }
    // READ — Get room by ID
    public Room getRoomById(int roomId) {
        String sql = """
            SELECT r.*, u.username AS host_username
            FROM rooms r
            JOIN users u ON r.host_user_id = u.user_id
            WHERE r.room_id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRowToRoom(rs);
        } catch (SQLException e) {
            System.err.println("[RoomDAO] getRoomById error: " + e.getMessage());
        }
        return null;
    }

    // READ — Get all active rooms
    public List<Room> getActiveRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = """
            SELECT r.*, u.username AS host_username
            FROM rooms r
            JOIN users u ON r.host_user_id = u.user_id
            WHERE r.is_active = 1
            ORDER BY r.created_at DESC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapRowToRoom(rs));
        } catch (SQLException e) {
            System.err.println("[RoomDAO] getActiveRooms error: " + e.getMessage());
        }
        return rooms;
    }


    // UPDATE — Close a room (host ends session)
    public boolean closeRoom(int roomId) {
        String sql = "UPDATE rooms SET is_active = 0, closed_at = NOW() WHERE room_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            boolean success = ps.executeUpdate() > 0;
            if (success) System.out.println("[RoomDAO] Room closed: " + roomId);
            return success;
        } catch (SQLException e) {
            System.err.println("[RoomDAO] closeRoom error: " + e.getMessage());
        }
        return false;
    }


    // HELPER — Generate a random 6-char room code

    private String generateRoomCode() {
        // Take first 6 chars of a UUID (uppercase, no dashes)
        return UUID.randomUUID().toString()
                   .replace("-", "")
                   .substring(0, 6)
                   .toUpperCase();
    }
    // HELPER — Map ResultSet row → Room object
    private Room mapRowToRoom(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getInt("room_id"));
        r.setRoomCode(rs.getString("room_code"));
        r.setRoomName(rs.getString("room_name"));
        r.setHostUserId(rs.getInt("host_user_id"));
        r.setVideoPath(rs.getString("video_path"));
        r.setActive(rs.getBoolean("is_active"));
        r.setMaxUsers(rs.getInt("max_users"));
        r.setHostUsername(rs.getString("host_username"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());

        Timestamp closed = rs.getTimestamp("closed_at");
        if (closed != null) r.setClosedAt(closed.toLocalDateTime());

        return r;
    }
}
