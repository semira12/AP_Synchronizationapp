package com.syncstream.server;

import com.syncstream.api.DatabaseAPI;
import com.syncstream.model.Room;
import com.syncstream.model.User;

import java.io.*;
import java.net.Socket;


public class ClientHandler implements Runnable {

    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;


    private User currentUser;
    private int currentRoomId = -1;
    private int watchHistoryId = -1;
    private long joinTime;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                handleCommand(line.trim());
            }
        } catch (IOException e) {
            System.out.println("[Handler] Client disconnected: " + getUsername());
        } finally {
            cleanup();
        }
    }

    private void handleCommand(String raw) {
        if (raw.isEmpty()) return;
        String[] parts = raw.split(":", 4);
        String cmd = parts[0].toUpperCase();

        switch (cmd) {
            case "LOGIN"       -> handleLogin(parts);
            case "REGISTER"    -> handleRegister(parts);
            case "CREATE_ROOM" -> handleCreateRoom(parts);
            case "JOIN_ROOM"   -> handleJoinRoom(parts);
            case "CHAT"        -> handleChat(parts);
            case "REACTION"    -> handleReaction(parts);
            case "PLAY"        -> handlePlay(parts);
            case "PAUSE"       -> handlePause(parts);
            case "SEEK"        -> handleSeek(parts);
            case "LEAVE"       -> handleLeave();
            default            -> send("ERROR:Unknown command: " + cmd);
        }
    }


    private void handleLogin(String[] parts) {
        if (parts.length < 3) { send("ERROR:LOGIN format: LOGIN:<user>:<pass>"); return; }
        User user = DatabaseAPI.login(parts[1], parts[2]);
        if (user != null) {
            this.currentUser = user;
            send("OK:LOGIN:" + user.getUserId() + ":" + user.getUsername());
            System.out.println("[Handler] Logged in: " + user.getUsername());
        } else {
            send("ERROR:Invalid username or password");
        }
    }

    private void handleRegister(String[] parts) {
        if (parts.length < 4) { send("ERROR:REGISTER format: REGISTER:<user>:<email>:<pass>"); return; }
        int userId = DatabaseAPI.register(parts[1], parts[2], parts[3]);
        if (userId == -2)     send("ERROR:Username already taken");
        else if (userId < 0)  send("ERROR:Registration failed");
        else                  send("OK:REGISTER:" + userId);
    }

    private void handleCreateRoom(String[] parts) {
        if (!isLoggedIn()) return;
        if (parts.length < 3) { send("ERROR:CREATE_ROOM format: CREATE_ROOM:<name>:<videoPath>"); return; }
        int roomId = DatabaseAPI.createRoom(parts[1], currentUser.getUserId(), parts[2]);
        if (roomId > 0) {
            joinRoomInternal(roomId, parts[2]);
            var rooms = DatabaseAPI.getActiveRooms();
            String code = rooms.stream()
                .filter(r -> r.getRoomId() == roomId)
                .map(r -> r.getRoomCode())
                .findFirst().orElse("?");
            send("OK:CREATE_ROOM:" + roomId + ":" + code);
        } else {
            send("ERROR:Could not create room");
        }
    }

    private void handleJoinRoom(String[] parts) {
        if (!isLoggedIn()) return;
        if (parts.length < 2) { send("ERROR:JOIN_ROOM format: JOIN_ROOM:<roomCode>"); return; }
        Room room = DatabaseAPI.joinRoomByCode(parts[1], currentUser.getUserId());
        if (room != null) {
            joinRoomInternal(room.getRoomId(), room.getVideoPath());
            send("OK:JOIN_ROOM:" + room.getRoomId() + ":" + room.getRoomName() + ":" + room.getVideoPath());
            // Notify others in the room
            SyncServer.broadcastExcludeSender(currentRoomId,
                "USER_JOINED:" + currentUser.getUsername(), this);
        } else {
            send("ERROR:Room not found or full");
        }
    }

    private void handleChat(String[] parts) {
        if (!isLoggedIn() || !isInRoom()) return;
        String message = parts.length >= 2 ? raw(parts, 1) : "";
        if (message.isEmpty()) return;


        DatabaseAPI.saveMessage(currentRoomId, currentUser.getUserId(), message, "text");


        SyncServer.broadcastToRoom(currentRoomId,
            "CHAT:" + currentUser.getUsername() + ":" + message, this);
    }

    private void handleReaction(String[] parts) {
        if (!isLoggedIn() || !isInRoom()) return;
        String emoji = parts.length >= 2 ? parts[1] : "👍";

        DatabaseAPI.saveMessage(currentRoomId, currentUser.getUserId(), emoji, "reaction");
        SyncServer.broadcastToRoom(currentRoomId,
            "REACTION:" + currentUser.getUsername() + ":" + emoji, this);
    }

    private void handlePlay(String[] parts) {
        if (!isLoggedIn() || !isInRoom()) return;
        String ts = parts.length >= 2 ? parts[1] : "0";
        SyncServer.broadcastExcludeSender(currentRoomId, "SYNC_PLAY:" + ts, this);
        send("OK:PLAY");
    }

    private void handlePause(String[] parts) {
        if (!isLoggedIn() || !isInRoom()) return;
        String ts = parts.length >= 2 ? parts[1] : "0";
        SyncServer.broadcastExcludeSender(currentRoomId, "SYNC_PAUSE:" + ts, this);
        send("OK:PAUSE");
    }

    private void handleSeek(String[] parts) {
        if (!isLoggedIn() || !isInRoom()) return;
        String ts = parts.length >= 2 ? parts[1] : "0";
        SyncServer.broadcastToRoom(currentRoomId, "SYNC_SEEK:" + ts, this);
        send("OK:SEEK");
    }

    private void handleLeave() {
        if (currentRoomId >= 0) {
            SyncServer.broadcastExcludeSender(currentRoomId,
                "USER_LEFT:" + getUsername(), this);
            endWatchSession();
            DatabaseAPI.leaveRoom(currentRoomId, currentUser.getUserId());
            SyncServer.leaveRoom(currentRoomId, this);
            currentRoomId = -1;
        }
        send("OK:LEAVE");
    }

    private void joinRoomInternal(int roomId, String videoPath) {
        this.currentRoomId = roomId;
        this.joinTime = System.currentTimeMillis();
        SyncServer.joinRoom(roomId, this);
        this.watchHistoryId = DatabaseAPI.startWatchSession(
            currentUser.getUserId(), roomId, videoPath);
    }

    private void endWatchSession() {
        if (watchHistoryId > 0) {
            int seconds = (int)((System.currentTimeMillis() - joinTime) / 1000);
            DatabaseAPI.endWatchSession(watchHistoryId, seconds);
            watchHistoryId = -1;
        }
    }

    private boolean isLoggedIn() {
        if (currentUser == null) { send("ERROR:Not logged in"); return false; }
        return true;
    }

    private boolean isInRoom() {
        if (currentRoomId < 0) { send("ERROR:Not in a room"); return false; }
        return true;
    }


    private String raw(String[] parts, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < parts.length; i++) {
            if (i > from) sb.append(":");
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    private void cleanup() {
        try {
            if (currentRoomId >= 0 && currentUser != null) {
                SyncServer.broadcastExcludeSender(currentRoomId,
                    "USER_LEFT:" + getUsername(), this);
                endWatchSession();
                DatabaseAPI.leaveRoom(currentRoomId, currentUser.getUserId());
                SyncServer.leaveRoom(currentRoomId, this);
            } else {
                SyncServer.leaveRoom(-1, this); // just remove from allClients
            }
            socket.close();
        } catch (IOException ignored) {}
    }
    public synchronized void send(String message) {
        if (out != null) out.println(message);
    }

    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "unknown";
    }
}
