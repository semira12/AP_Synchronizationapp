package com.syncstream.model;

import java.time.LocalDateTime;

public class WatchHistory {

    private int    historyId;
    private int    userId;
    private int    roomId;
    private String videoPath;
    private LocalDateTime watchStart;
    private LocalDateTime watchEnd;
    private int    totalSeconds;

    private String username;
    private String roomName;

    public WatchHistory() {}

    public WatchHistory(int userId, int roomId, String videoPath) {
        this.userId    = userId;
        this.roomId    = roomId;
        this.videoPath = videoPath;
    }

    public int getHistoryId()          { return historyId; }
    public int getUserId()             { return userId; }
    public int getRoomId()             { return roomId; }
    public String getVideoPath()       { return videoPath; }
    public LocalDateTime getWatchStart() { return watchStart; }
    public LocalDateTime getWatchEnd()   { return watchEnd; }
    public int getTotalSeconds()       { return totalSeconds; }
    public String getUsername()        { return username; }
    public String getRoomName()        { return roomName; }

    public void setHistoryId(int id)           { this.historyId    = id; }
    public void setUserId(int id)              { this.userId       = id; }
    public void setRoomId(int id)              { this.roomId       = id; }
    public void setVideoPath(String path)      { this.videoPath    = path; }
    public void setWatchStart(LocalDateTime t) { this.watchStart   = t; }
    public void setWatchEnd(LocalDateTime t)   { this.watchEnd     = t; }
    public void setTotalSeconds(int s)         { this.totalSeconds = s; }
    public void setUsername(String name)       { this.username     = name; }
    public void setRoomName(String name)       { this.roomName     = name; }


    public String getFormattedDuration() {
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;
        if (h > 0) return h + "h " + m + "m " + s + "s";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }
}
