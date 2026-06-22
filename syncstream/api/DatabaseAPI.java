package com.syncstream.api;

import com.syncstream.dao.*; // ParticipantDAO, ChatMessageDAO, WatchHistoryDAO
import com.syncstream.model.*;
import com.syncstream.util.FileUtil;

import java.util.List;

/**
 *   Person 1 (GUI) and Person 2 (Server) should NOT instantiate DAOs directly.
 *   They call DatabaseAPI.register(), DatabaseAPI.login(), etc.
 *   This keeps the code clean and decoupled.

 */
public class DatabaseAPI {


    private static final UserDAO         userDAO         = new UserDAO();
    private static final RoomDAO         roomDAO         = new RoomDAO();
    private static final ParticipantDAO participantDAO  = new ParticipantDAO();
    private static final ChatMessageDAO chatMessageDAO  = new ChatMessageDAO();
    private static final WatchHistoryDAO watchHistoryDAO = new WatchHistoryDAO();
    // USER METHODS


    public static int register(String username, String email, String password) {
        if (userDAO.usernameExists(username)) {
            System.out.println("[API] Username already taken: " + username);
            return -2;
        }
        return userDAO.registerUser(username, email, password);
    }

    public static User login(String username, String password) {
        return userDAO.login(username, password);
    }


    public static User getUser(int userId) {
        return userDAO.getUserById(userId);
    }
    // ROOM METHODS

    public static int createRoom(String roomName, int hostUserId, String videoPath) {
        int roomId = roomDAO.createRoom(roomName, hostUserId, videoPath);
        if (roomId > 0) {
            // Automatically add host as a participant with role "host"
            participantDAO.joinRoom(roomId, hostUserId, "host");
        }
        return roomId;
    }


    public static Room joinRoomByCode(String roomCode, int userId) {
        Room room = roomDAO.getRoomByCode(roomCode);
        if (room == null) {
            System.out.println("[API] Room not found: " + roomCode);
            return null;
        }
        int count = participantDAO.getActiveCount(room.getRoomId());
        if (count >= room.getMaxUsers()) {
            System.out.println("[API] Room is full: " + roomCode);
            return null;
        }
        participantDAO.joinRoom(room.getRoomId(), userId, "viewer");
        return room;
    }


    public static boolean closeRoom(int roomId) {
        return roomDAO.closeRoom(roomId);
    }


    public static List<Room> getActiveRooms() {
        return roomDAO.getActiveRooms();
    }

    // PARTICIPANT METHODS



    public static boolean leaveRoom(int roomId, int userId) {
        return participantDAO.leaveRoom(roomId, userId);
    }


    public static int getParticipantCount(int roomId) {
        return participantDAO.getActiveCount(roomId);
    }


    // CHAT METHODS


    public static int saveMessage(int roomId, int userId, String message, String msgType) {
        return chatMessageDAO.saveMessage(roomId, userId, message, msgType);
    }


    public static List<ChatMessage> getChatHistory(int roomId) {
        return chatMessageDAO.getMessagesByRoom(roomId);
    }


    public static String exportChatLog(int roomId) {
        String logContent = chatMessageDAO.exportChatLog(roomId);
        return FileUtil.saveChatLog(roomId, logContent);
    }


    // WATCH HISTORY METHODS

    public static int startWatchSession(int userId, int roomId, String videoPath) {
        return watchHistoryDAO.startSession(userId, roomId, videoPath);
    }

    public static boolean endWatchSession(int historyId, int totalSeconds) {
        return watchHistoryDAO.endSession(historyId, totalSeconds);
    }


    public static List<WatchHistory> getWatchHistory(int userId) {
        return watchHistoryDAO.getHistoryByUser(userId);
    }
}
