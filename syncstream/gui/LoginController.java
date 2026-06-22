package com.syncstream.gui;

import com.syncstream.client.ServerConnection;
import com.syncstream.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class LoginController {


    @FXML private TextField     hostField;
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     emailField;       // only visible during registration
    @FXML private Button        loginButton;
    @FXML private Button        registerButton;
    @FXML private Label         statusLabel;
    @FXML private Label         titleLabel;
    @FXML private HBox          emailRow;
    @FXML private Label         emailLabel;

    private boolean isRegisterMode = false;
    private ServerConnection conn;

    @FXML
    public void initialize() {
        emailRow.setVisible(false);
        emailRow.setManaged(false);
        hostField.setText("localhost");
        statusLabel.setText("");
    }


    @FXML
    private void onLogin() {
        String host = hostField.getText().trim();
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        statusLabel.setText("Connecting...");
        loginButton.setDisable(true);


        new Thread(() -> {
            try {
                conn = new ServerConnection(host, 5050, this::onServerMessage);
                conn.send("LOGIN:" + user + ":" + pass);
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Cannot connect to server at " + host + ":5050");
                    loginButton.setDisable(false);
                });
            }
        }).start();
    }


    @FXML
    private void onRegister() {
        if (!isRegisterMode) {
            // Switch to register mode
            isRegisterMode = true;
            titleLabel.setText("Create Account");
            emailRow.setVisible(true);
            emailRow.setManaged(true);
            loginButton.setText("Login");
            registerButton.setText("Register Now");
            return;
        }


        String host  = hostField.getText().trim();
        String user  = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String pass  = passwordField.getText();

        if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        statusLabel.setText("Registering...");
        registerButton.setDisable(true);

        new Thread(() -> {
            try {
                conn = new ServerConnection(host, 5050, this::onServerMessage);
                conn.send("REGISTER:" + user + ":" + email + ":" + pass);
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Cannot connect to server.");
                    registerButton.setDisable(false);
                });
            }
        }).start();
    }

    // SERVER MESSAGE HANDLER
    private void onServerMessage(String msg) {
        Platform.runLater(() -> {
            if (msg.startsWith("OK:LOGIN:")) {
                // Format: OK:LOGIN:<userId>:<username>
                String[] parts = msg.split(":");
                int userId   = Integer.parseInt(parts[2]);
                String uname = parts[3];

                // Build a lightweight User for the session
                User user = new User();
                user.setUserId(userId);
                user.setUsername(uname);

                SessionState session = SessionState.getInstance();
                session.setCurrentUser(user);
                session.setConnection(conn);

                try { MainApp.showLobby(); }
                catch (Exception e) { statusLabel.setText("Error loading lobby."); }

            } else if (msg.startsWith("OK:REGISTER:")) {
                statusLabel.setText("Registration successful! Please login.");
                isRegisterMode = false;
                emailRow.setVisible(false);
                emailRow.setManaged(false);
                titleLabel.setText("Login");
                loginButton.setText("Login");
                registerButton.setText("Register");
                registerButton.setDisable(false);

            } else if (msg.startsWith("ERROR:")) {
                statusLabel.setText(msg.substring(6));
                loginButton.setDisable(false);
                registerButton.setDisable(false);
            }
        });
    }
}
