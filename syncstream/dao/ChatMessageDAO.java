package com.syncstream.dao;

import com.syncstream.db.DBConnection;
import com.syncstream.model.ChatMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageDAO {

    private final Connection conn;

    public ChatMessageDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    public int saveMessage(int roomId, int userId, String message, String msgType) {
        String sql = "INSERT INTO chat_messages (room_id, user_id, message, msg_type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, roomId);
            ps.setInt(2, userId);
            ps.setString(3, message);
            ps.setString(4, msgType);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ChatMessageDAO] saveMessage error: " + e.getMessage());
        }
        return -1;
    }

    public List<ChatMessage> getMessagesByRoom(int roomId) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = """
            SELECT cm.*, u.username
            FROM chat_messages cm
            JOIN users u ON cm.user_id = u.user_id
            WHERE cm.room_id = ?
            ORDER BY cm.sent_at ASC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChatMessage msg = new ChatMessage();
                msg.setMessageId(rs.getInt("message_id"));
                msg.setRoomId(rs.getInt("room_id"));
                msg.setUserId(rs.getInt("user_id"));
                msg.setMessage(rs.getString("message"));
                msg.setMsgType(rs.getString("msg_type"));
                msg.setUsername(rs.getString("username"));
                Timestamp ts = rs.getTimestamp("sent_at");
                if (ts != null) msg.setSentAt(ts.toLocalDateTime());
                messages.add(msg);
            }
        } catch (SQLException e) {
            System.err.println("[ChatMessageDAO] getMessagesByRoom error: " + e.getMessage());
        }
        return messages;
    }

    public String exportChatLog(int roomId) {
        List<ChatMessage> messages = getMessagesByRoom(roomId);
        StringBuilder sb = new StringBuilder();
        sb.append("=== Chat Log for Room ID: ").append(roomId).append(" ===\n\n");
        for (ChatMessage msg : messages) {
            sb.append("[").append(msg.getSentAt()).append("] ")
                    .append(msg.getUsername()).append(": ")
                    .append(msg.getMessage()).append("\n");
        }
        return sb.toString();
    }
}