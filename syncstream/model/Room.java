package com.syncstream.model;

import java.time.LocalDateTime;

public class Room {

    private int    roomId;
    private String roomCode;
    private String roomName;
    private int    hostUserId;
    private String videoPath;
    private boolean active;
    private int    maxUsers;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;


    private String hostUsername;

    public Room() {}

    public Room(String roomCode, String roomName, int hostUserId, String videoPath) {
        this.roomCode    = roomCode;
        this.roomName    = roomName;
        this.hostUserId  = hostUserId;
        this.videoPath   = videoPath;
        this.active      = true;
        this.maxUsers    = 10;
    }

    // ======================== GETTERS ========================
    public int getRoomId()           { return roomId; }
    public String getRoomCode()      { return roomCode; }
    public String getRoomName()      { return roomName; }
    public int getHostUserId()       { return hostUserId; }
    public String getVideoPath()     { return videoPath; }
    public boolean isActive()        { return active; }
    public int getMaxUsers()         { return maxUsers; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getClosedAt()  { return closedAt; }
    public String getHostUsername()  { return hostUsername; }

    // ======================== SETTERS ========================
    public void setRoomId(int roomId)           { this.roomId    = roomId; }
    public void setRoomCode(String roomCode)    { this.roomCode  = roomCode; }
    public void setRoomName(String roomName)    { this.roomName  = roomName; }
    public void setHostUserId(int id)           { this.hostUserId = id; }
    public void setVideoPath(String videoPath)  { this.videoPath = videoPath; }
    public void setActive(boolean active)       { this.active    = active; }
    public void setMaxUsers(int maxUsers)       { this.maxUsers  = maxUsers; }
    public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }
    public void setClosedAt(LocalDateTime t)    { this.closedAt  = t; }
    public void setHostUsername(String name)    { this.hostUsername = name; }

    @Override
    public String toString() {
        return "Room{id=" + roomId + ", code='" + roomCode + "', name='" + roomName + "'}";
    }
}
