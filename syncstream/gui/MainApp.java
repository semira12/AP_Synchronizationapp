package com.syncstream.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * MainApp — JavaFX Application entry point.
 *
 * SCREENS:
 *   login.fxml      → LoginController
 *   lobby.fxml      → LobbyController
 *   room.fxml       → RoomController  (video player + chat + sync controls)
 *
 * HOW TO RUN:
 *   Run MainApp.main() — this starts the GUI.
 *   Make sure the server (SyncServer.main()) is already running.
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("SyncStream — Watch Together");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Start on the Login screen
        showLogin();
        primaryStage.show();
    }

    /** Navigate to the Login / Register screen. */
    public static void showLogin() throws Exception {
        Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/login.fxml"));
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.setTitle("SyncStream — Login");
    }

    /** Navigate to the Lobby screen (create/join rooms). */
    public static void showLobby() throws Exception {
        Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/lobby.fxml"));
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.setTitle("SyncStream — Lobby");
    }

    /** Navigate to the Room screen (video player + chat). */
    public static void showRoom() throws Exception {
        Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/room.fxml"));
        primaryStage.setScene(new Scene(root, 1100, 700));
        primaryStage.setTitle("SyncStream — Watch Room");
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
