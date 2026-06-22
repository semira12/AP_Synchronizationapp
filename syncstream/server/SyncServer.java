package com.syncstream.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class SyncServer {

    public static final int PORT = 5050;

    // roomId → list of connected ClientHandlers in that room
    private static final Map<Integer, List<ClientHandler>> rooms = new ConcurrentHashMap<>();

    // All connected clients (for server-wide broadcast / admin)
    private static final List<ClientHandler> allClients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws IOException {

        System.out.println("  SyncStream Server starting on port " + PORT);
        System.out.println("===========================================");

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("[Server] Waiting for connections...\n");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("[Server] New connection: " + clientSocket.getInetAddress());
            ClientHandler handler = new ClientHandler(clientSocket);
            allClients.add(handler);
            new Thread(handler).start();
        }
    }


    public static synchronized void joinRoom(int roomId, ClientHandler client) {
        rooms.computeIfAbsent(roomId, k -> Collections.synchronizedList(new ArrayList<>())).add(client);
        System.out.println("[Server] User " + client.getUsername() + " joined room " + roomId
                + " (total: " + rooms.get(roomId).size() + ")");
    }

    public static synchronized void leaveRoom(int roomId, ClientHandler client) {
        List<ClientHandler> roomClients = rooms.get(roomId);
        if (roomClients != null) {
            roomClients.remove(client);
            System.out.println("[Server] User " + client.getUsername() + " left room " + roomId);
            if (roomClients.isEmpty()) rooms.remove(roomId);
        }
        allClients.remove(client);
    }



    public static void broadcastToRoom(int roomId, String message, ClientHandler sender) {
        List<ClientHandler> roomClients = rooms.get(roomId);
        if (roomClients == null) return;
        synchronized (roomClients) {
            for (ClientHandler client : roomClients) {
                client.send(message);
            }
        }
        System.out.println("[Server] Broadcast to room " + roomId + ": " + message.trim());
    }


    public static void broadcastExcludeSender(int roomId, String message, ClientHandler sender) {
        List<ClientHandler> roomClients = rooms.get(roomId);
        if (roomClients == null) return;
        synchronized (roomClients) {
            for (ClientHandler client : roomClients) {
                if (client != sender) {
                    client.send(message);
                }
            }
        }
    }


    public static int getRoomSize(int roomId) {
        List<ClientHandler> roomClients = rooms.get(roomId);
        return roomClients == null ? 0 : roomClients.size();
    }
}
