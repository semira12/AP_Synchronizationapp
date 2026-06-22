package com.syncstream.gui;

import com.syncstream.api.DatabaseAPI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;

public class RoomController {

    @FXML private MediaView    mediaView;
    @FXML private VBox         videoContainer;
    @FXML private Button       playButton;
    @FXML private Button       pauseButton;
    @FXML private Slider       seekSlider;
    @FXML private Label        timeLabel;
    @FXML private HBox         hostControls;
    @FXML private HBox         timelineRow;


    @FXML private ListView<String> chatList;
    @FXML private TextField        chatInput;
    @FXML private HBox             reactionBar;


    @FXML private Label roomInfoLabel;
    @FXML private Label participantCountLabel;

    private final SessionState session = SessionState.getInstance();
    private MediaPlayer mediaPlayer;
    private boolean seekSliderDragging = false;

    // INITIALIZATION
    @FXML
    public void initialize() {

        roomInfoLabel.setText("Room: " + session.getCurrentRoomName()
                + "  |  Code: " + session.getCurrentRoomCode()
                + "  |  " + (session.isHost() ? "👑 HOST" : "👤 VIEWER"));


        hostControls.setVisible(session.isHost());
        hostControls.setManaged(session.isHost());


        if (!session.isHost() && timelineRow != null) {
            timelineRow.setDisable(true);
        }


        session.getConnection().setListener(this::onServerMessage);


        loadVideo(session.getCurrentVideoPath());


        loadChatHistory();

        updateParticipantCount();
    }

    private void loadVideo(String videoPath) {
        if (videoPath == null || videoPath.isBlank()) {
            addSystemMessage("No video path specified.");
            return;
        }

        try {
            File f = new File(videoPath);
            if (!f.exists()) {
                addSystemMessage("Video file not found: " + videoPath);
                return;
            }

            Media media = new Media(f.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);


            if (videoContainer != null) {
                mediaView.fitWidthProperty().bind(videoContainer.widthProperty());

            }


            mediaPlayer.currentTimeProperty().addListener((obs, old, now) -> {
                if (!seekSliderDragging) {
                    double totalSecs = mediaPlayer.getTotalDuration() != null
                            ? mediaPlayer.getTotalDuration().toSeconds() : 0;
                    if (totalSecs > 0) {
                        double pct = now.toSeconds() / totalSecs * 100;
                        Platform.runLater(() -> {
                            seekSlider.setValue(pct);
                            timeLabel.setText(formatTime(now.toSeconds())
                                    + " / " + formatTime(totalSecs));
                        });
                    }
                }
            });


            mediaPlayer.setOnReady(() -> {
                Platform.runLater(() ->
                    addSystemMessage("✔ Video loaded: " + f.getName()
                            + " — " + formatTime(media.getDuration().toSeconds())));
                mediaPlayer.pause();
            });

            mediaPlayer.setOnError(() ->
                Platform.runLater(() ->
                    addSystemMessage("⚠ Media error: " + mediaPlayer.getError().getMessage())));

        } catch (Exception e) {
            addSystemMessage("Could not load video: " + e.getMessage());
        }
    }

    // HOST CONTROLS (only host can trigger these)
    @FXML
    private void onPlay() {
        if (mediaPlayer == null) return;
        mediaPlayer.play();
        int secs = (int) mediaPlayer.getCurrentTime().toSeconds();
        session.send("PLAY:" + secs);
    }

    @FXML
    private void onPause() {
        if (mediaPlayer == null) return;
        mediaPlayer.pause();
        int secs = (int) mediaPlayer.getCurrentTime().toSeconds();
        session.send("PAUSE:" + secs);
    }

    @FXML
    private void onSeekStart() { seekSliderDragging = true; }

    @FXML
    private void onSeekEnd() {
        seekSliderDragging = false;
        if (mediaPlayer == null || !session.isHost()) return;
        double totalSecs = mediaPlayer.getTotalDuration().toSeconds();
        double seekTo    = seekSlider.getValue() / 100.0 * totalSecs;
        mediaPlayer.seek(Duration.seconds(seekTo));
        session.send("SEEK:" + (int) seekTo);
    }

    // CHAT

    @FXML
    private void onSendChat() {
        String text = chatInput.getText().trim();
        if (text.isEmpty()) return;
        session.send("CHAT:" + text);
        chatInput.clear();

    }

    @FXML private void onReact1() { session.send("REACTION:👍"); }
    @FXML private void onReact2() { session.send("REACTION:❤️"); }
    @FXML private void onReact3() { session.send("REACTION:😂"); }
    @FXML private void onReact4() { session.send("REACTION:😮"); }
    @FXML private void onReact5() { session.send("REACTION:😢"); }


    @FXML
    private void onLeaveRoom() {
        if (mediaPlayer != null) mediaPlayer.stop();
        session.send("LEAVE");
        try { MainApp.showLobby(); }
        catch (Exception e) { e.printStackTrace(); }
    }


    public void onServerMessage(String msg) {
        Platform.runLater(() -> {
            if (msg.startsWith("CHAT:")) {
                // CHAT:<username>:<message>  (message may contain colons)
                String[] p = msg.split(":", 3);
                if (p.length >= 3) {
                    chatList.getItems().add("[" + p[1] + "] " + p[2]);
                    chatList.scrollTo(chatList.getItems().size() - 1);
                }

            } else if (msg.startsWith("REACTION:")) {
                // REACTION:<username>:<emoji>
                String[] p = msg.split(":", 3);
                if (p.length >= 3) {
                    chatList.getItems().add(p[1] + " reacted: " + p[2]);
                    chatList.scrollTo(chatList.getItems().size() - 1);
                }

            } else if (msg.startsWith("SYNC_PLAY:")) {
                int secs = Integer.parseInt(msg.split(":")[1]);
                syncPlay(secs);

            } else if (msg.startsWith("SYNC_PAUSE:")) {
                int secs = Integer.parseInt(msg.split(":")[1]);
                syncPause(secs);

            } else if (msg.startsWith("SYNC_SEEK:")) {
                int secs = Integer.parseInt(msg.split(":")[1]);
                syncSeek(secs);

            } else if (msg.startsWith("USER_JOINED:")) {
                String user = msg.split(":", 2)[1];
                addSystemMessage("▶ " + user + " joined the room");
                updateParticipantCount();

            } else if (msg.startsWith("USER_LEFT:")) {
                String user = msg.split(":", 2)[1];
                addSystemMessage("◀ " + user + " left the room");
                updateParticipantCount();

            } else if (msg.equals("ROOM_CLOSED")) {
                addSystemMessage("⚠ The host has closed the room.");
                if (mediaPlayer != null) mediaPlayer.stop();

            } else if (msg.startsWith("OK:")) {
                // Acknowledge-only replies (OK:PLAY, OK:PAUSE, OK:SEEK, OK:LEAVE)
                // No UI action needed — suppress so they don't clutter chat
                System.out.println("[Room] Server ACK: " + msg);

            } else if (msg.startsWith("ERROR:")) {
                addSystemMessage("⚠ " + msg.substring(6));
            }
        });
    }


    private void syncPlay(int atSeconds) {
        if (mediaPlayer == null) return;
        mediaPlayer.seek(Duration.seconds(atSeconds));
        mediaPlayer.play();
        addSystemMessage("▶ Host resumed playback at " + formatTime(atSeconds));
    }

    private void syncPause(int atSeconds) {
        if (mediaPlayer == null) return;
        mediaPlayer.seek(Duration.seconds(atSeconds));
        mediaPlayer.pause();
        addSystemMessage("⏸ Host paused at " + formatTime(atSeconds));
    }

    private void syncSeek(int atSeconds) {
        if (mediaPlayer == null) return;
        mediaPlayer.seek(Duration.seconds(atSeconds));
        addSystemMessage("⏩ Host seeked to " + formatTime(atSeconds));
    }


    private void addSystemMessage(String text) {
        chatList.getItems().add("★ " + text);
        chatList.scrollTo(chatList.getItems().size() - 1);
    }

    private void loadChatHistory() {
        new Thread(() -> {
            var history = DatabaseAPI.getChatHistory(session.getCurrentRoomId());
            Platform.runLater(() -> {
                if (!history.isEmpty()) {
                    addSystemMessage("── Chat history ──");
                    for (var m : history) {
                        chatList.getItems().add("[" + m.getUsername() + "] " + m.getMessage());
                    }
                    addSystemMessage("── Live ──");
                    chatList.scrollTo(chatList.getItems().size() - 1);
                }
            });
        }).start();
    }

    private void updateParticipantCount() {
        new Thread(() -> {
            int count = DatabaseAPI.getParticipantCount(session.getCurrentRoomId());
            Platform.runLater(() -> participantCountLabel.setText("👥 " + count + " watching"));
        }).start();
    }

    private String formatTime(double totalSeconds) {
        int secs = (int) totalSeconds;
        int h = secs / 3600;
        int m = (secs % 3600) / 60;
        int s = secs % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, s);
        return String.format("%d:%02d", m, s);
    }
}
