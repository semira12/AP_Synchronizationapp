package com.syncstream.gui;

import com.syncstream.client.ServerConnection;
import com.syncstream.model.User;

public class SessionState {

    private static SessionState instance;

    private ServerConnection connection;
    private User currentUser;
    private int currentRoomId;
    private String currentRoomName;
    private String currentRoomCode;
    private String currentVideoPath;
    private boolean isHost;

    private SessionState() {}

    public static SessionState getInstance() {
        if (instance == null) instance = new SessionState();
        return instance;
    }

    // =================== Getters / Setters ===================

    public ServerConnection getConnection()  { return connection; }
    public void setConnection(ServerConnection c) { this.connection = c; }

    public User getCurrentUser()           { return currentUser; }
    public void setCurrentUser(User u)     { this.currentUser = u; }

    public int getCurrentRoomId()          { return currentRoomId; }
    public void setCurrentRoomId(int id)   { this.currentRoomId = id; }

    public String getCurrentRoomName()     { return currentRoomName; }
    public void setCurrentRoomName(String n) { this.currentRoomName = n; }

    public String getCurrentRoomCode()     { return currentRoomCode; }
    public void setCurrentRoomCode(String c) { this.currentRoomCode = c; }

    public String getCurrentVideoPath()    { return currentVideoPath; }
    public void setCurrentVideoPath(String p) { this.currentVideoPath = p; }

    public boolean isHost()                { return isHost; }
    public void setHost(boolean host)      { this.isHost = host; }

    /** Send a command to the server. */
    public void send(String command) {
        if (connection != null) connection.send(command);
    }

    public void reset() {
        instance = new SessionState();
    }
}
