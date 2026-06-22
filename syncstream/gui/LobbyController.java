package com.syncstream.gui;

import com.syncstream.api.DatabaseAPI;
import com.syncstream.model.Room;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class LobbyController {

    @FXML private Label        welcomeLabel;
    @FXML private TextField    roomNameField;
    @FXML private TextField    videoPathField;
    @FXML private TextField    roomCodeField;
    @FXML private ListView<String> activeRoomsList;
    @FXML private Label        statusLabel;

    private final SessionState session = SessionState.getInstance();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + session.getCurrentUser().getUsername() + "!");
        statusLabel.setText("");


        session.getConnection().setListener(this::onServerMessage);

        loadActiveRooms();
    }

    private void loadActiveRooms() {
        new Thread(() -> {
            List<Room> rooms = DatabaseAPI.getActiveRooms();
            Platform.runLater(() -> {
                activeRoomsList.getItems().clear();
                if (rooms.isEmpty()) {
                    activeRoomsList.getItems().add("No active rooms yet. Create one!");
                } else {
                    for (Room r : rooms) {
                        activeRoomsList.getItems().add(
                            String.format("[%s] %s (Host: %s, %d/%d users)",
                                r.getRoomCode(), r.getRoomName(),
                                r.getHostUsername(),
                                DatabaseAPI.getParticipantCount(r.getRoomId()),
                                r.getMaxUsers())
                        );
                    }
                }
            });
        }).start();
    }


    // CREATE ROOM

    @FXML
    private void onBrowseVideo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Video File");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mkv", "*.avi", "*.mov"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = fc.showOpenDialog(MainApp.getPrimaryStage());
        if (file != null) {
            videoPathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void onCreateRoom() {
        String name = roomNameField.getText().trim();
        String path = videoPathField.getText().trim();

        if (name.isEmpty() || path.isEmpty()) {
            statusLabel.setText("Please enter a room name and select a video.");
            return;
        }

        if (!new File(path).exists()) {
            statusLabel.setText("Video file not found. Please check the path.");
            return;
        }

        statusLabel.setText("Creating room...");
        session.send("CREATE_ROOM:" + name + ":" + path);
    }


    // JOIN ROOM

    @FXML
    private void onJoinRoom() {
        String code = roomCodeField.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            statusLabel.setText("Please enter a room code.");
            return;
        }
        statusLabel.setText("Joining room " + code + "...");
        session.send("JOIN_ROOM:" + code);
    }

    @FXML
    private void onRefreshRooms() {
        loadActiveRooms();
    }

    @FXML
    private void onLogout() {
        session.getConnection().disconnect();
        session.reset();
        try { MainApp.showLogin(); }
        catch (Exception e) { statusLabel.setText("Error."); }
    }

    // SERVER RESPONSE HANDLER

    public void onServerMessage(String msg) {
        Platform.runLater(() -> {
            if (msg.startsWith("OK:CREATE_ROOM:")) {

                String[] parts = msg.split(":");
                session.setCurrentRoomId(Integer.parseInt(parts[2]));
                session.setCurrentRoomCode(parts[3]);
                session.setCurrentRoomName(roomNameField.getText().trim());
                session.setCurrentVideoPath(videoPathField.getText().trim());
                session.setHost(true);
                try { MainApp.showRoom(); }
                catch (Exception e) { statusLabel.setText("Error loading room."); }

            } else if (msg.startsWith("OK:JOIN_ROOM:")) {

                String[] parts = msg.split(":", 5);
                session.setCurrentRoomId(Integer.parseInt(parts[2]));
                session.setCurrentRoomName(parts[3]);
                session.setCurrentVideoPath(parts[4]);
                session.setCurrentRoomCode(roomCodeField.getText().trim().toUpperCase());
                session.setHost(false);
                try { MainApp.showRoom(); }
                catch (Exception e) { statusLabel.setText("Error loading room."); }

            } else if (msg.startsWith("ERROR:")) {
                statusLabel.setText(msg.substring(6));
            }
        });
    }
}
