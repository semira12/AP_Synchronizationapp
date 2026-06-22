package com.syncstream.model;

import java.time.LocalDateTime;

public class User {

    private int    userId;
    private String username;
    private String email;
    private String password;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean active;


    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email    = email;
        this.password = password;
        this.active   = true;
    }

    // ======================== GETTERS ========================

    public int getUserId()           { return userId; }
    public String getUsername()      { return username; }
    public String getEmail()         { return email; }
    public String getPassword()      { return password; }
    public String getAvatarUrl()     { return avatarUrl; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getLastLogin()  { return lastLogin; }
    public boolean isActive()        { return active; }

    // ======================== SETTERS ========================

    public void setUserId(int userId)            { this.userId   = userId; }
    public void setUsername(String username)     { this.username = username; }
    public void setEmail(String email)           { this.email    = email; }
    public void setPassword(String password)     { this.password = password; }
    public void setAvatarUrl(String avatarUrl)   { this.avatarUrl = avatarUrl; }
    public void setCreatedAt(LocalDateTime t)    { this.createdAt = t; }
    public void setLastLogin(LocalDateTime t)    { this.lastLogin = t; }
    public void setActive(boolean active)        { this.active   = active; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", username='" + username + "', email='" + email + "'}";
    }
}
