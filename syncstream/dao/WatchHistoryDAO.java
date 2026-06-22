package com.syncstream.dao;

import com.syncstream.db.DBConnection;
import com.syncstream.model.WatchHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WatchHistoryDAO {

    private final Connection conn;

    public WatchHistoryDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    public int startSession(int userId, int roomId, String videoPath) {
        String sql = "INSERT INTO watch_history (user_id, room_id, video_path) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, roomId);
            ps.setString(3, videoPath);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("[WatchHistoryDAO] startSession error: " + e.getMessage());
        }
        return -1;
    }

    public boolean endSession(int historyId, int totalSeconds) {
        String sql = "UPDATE watch_history SET watch_end = NOW(), total_seconds = ? WHERE history_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, totalSeconds);
            ps.setInt(2, historyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[WatchHistoryDAO] endSession error: " + e.getMessage());
        }
        return false;
    }

    public List<WatchHistory> getHistoryByUser(int userId) {
        List<WatchHistory> list = new ArrayList<>();
        String sql = """
            SELECT wh.*, r.room_name
            FROM watch_history wh
            JOIN rooms r ON wh.room_id = r.room_id
            WHERE wh.user_id = ?
            ORDER BY wh.watch_start DESC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                WatchHistory wh = new WatchHistory();
                wh.setHistoryId(rs.getInt("history_id"));
                wh.setUserId(rs.getInt("user_id"));
                wh.setRoomId(rs.getInt("room_id"));
                wh.setVideoPath(rs.getString("video_path"));
                wh.setRoomName(rs.getString("room_name"));
                wh.setTotalSeconds(rs.getInt("total_seconds"));
                Timestamp start = rs.getTimestamp("watch_start");
                if (start != null) wh.setWatchStart(start.toLocalDateTime());
                Timestamp end = rs.getTimestamp("watch_end");
                if (end != null) wh.setWatchEnd(end.toLocalDateTime());
                list.add(wh);
            }
        } catch (SQLException e) {
            System.err.println("[WatchHistoryDAO] getHistoryByUser error: " + e.getMessage());
        }
        return list;
    }
}