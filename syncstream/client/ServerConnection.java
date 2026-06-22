package com.syncstream.client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;


public class ServerConnection {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // volatile so the reader thread always sees the latest value
    private volatile Consumer<String> messageListener;
    private volatile boolean running = false;

    public ServerConnection(String host, int port, Consumer<String> listener) throws IOException {
        this.messageListener = listener;
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(new BufferedWriter(
                   new OutputStreamWriter(socket.getOutputStream())), true);
        this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.running = true;

        Thread reader = new Thread(this::readLoop, "ServerReader");
        reader.setDaemon(true);
        reader.start();

        System.out.println("[Client] Connected to server at " + host + ":" + port);
    }


    public void setListener(Consumer<String> listener) {
        this.messageListener = listener;
    }

    private void readLoop() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                final String msg = line;
                Consumer<String> handler = messageListener;
                if (handler != null) handler.accept(msg);
            }
        } catch (IOException e) {
            if (running) {
                Consumer<String> handler = messageListener;
                if (handler != null) handler.accept("ERROR:Connection lost");
            }
        }
        running = false;
    }

    public void send(String message) {
        if (out != null && running) {
            out.println(message);
        }
    }

    public void disconnect() {
        running = false;
        try {
            if (out != null) out.println("LEAVE");
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        System.out.println("[Client] Disconnected from server.");
    }

    public boolean isConnected() {
        return running && socket != null && !socket.isClosed();
    }
}
