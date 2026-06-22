package com.syncstream.model;

import java.time.LocalDateTime;

public class ChatMessage {

    private int    messageId;
    private int    roomId;
    private int    userId;
    private String message;
    private String msgType;   // "text", "reaction", "system"
    private LocalDateTime sentAt;

    // Extra field (joined from users)
    private String username;

    public ChatMessage() {}

    public ChatMessage(int roomId, int userId, String message, String msgType) {
        this.roomId   = roomId;
        this.userId   = userId;
        this.message  = message;
        this.msgType  = msgType;
    }

    public int getMessageId()        { return messageId; }
    public int getRoomId()           { return roomId; }
    public int getUserId()           { return userId; }
    public String getMessage()       { return message; }
    public String getMsgType()       { return msgType; }
    public LocalDateTime getSentAt() { return sentAt; }
    public String getUsername()      { return username; }

    public void setMessageId(int id)        { this.messageId = id; }
    public void setRoomId(int roomId)       { this.roomId    = roomId; }
    public void setUserId(int userId)       { this.userId    = userId; }
    public void setMessage(String msg)      { this.message   = msg; }
    public void setMsgType(String type)     { this.msgType   = type; }
    public void setSentAt(LocalDateTime t)  { this.sentAt    = t; }
    public void setUsername(String name)    { this.username  = name; }

    @Override
    public String toString() {
        return "[" + sentAt + "] " + username + ": " + message;
    }
}
