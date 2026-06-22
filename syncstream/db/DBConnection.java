package com.syncstream.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    // --- Configuration ---
    private static final String URL      = "jdbc:mysql://localhost:3306/syncstream_db";
    private static final String USER     = "root";        // change to your MySQL user
    private static final String PASSWORD = ""; // change to your password

    // The single instance of this class
    private static DBConnection instance;

    // The actual JDBC connection object
    private Connection connection;

    // Private constructor: no one outside can do "new DBConnection()"
    private DBConnection() {
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connected to MySQL successfully.");

        } catch (ClassNotFoundException e) {

            System.err.println("[DB] MySQL Driver not found! Add mysql-connector-j.jar to your project.");
            e.printStackTrace();
        } catch (SQLException e) {

            System.err.println("[DB] Failed to connect: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {

            if (connection == null || !connection.isValid(2)) {
                System.out.println("[DB] Reconnecting...");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Reconnect failed: " + e.getMessage());
        }
        return connection;
    }


    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
