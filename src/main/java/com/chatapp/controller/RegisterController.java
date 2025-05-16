package com.chatapp.controller;

import com.chatapp.util.DatabaseUtil;
import com.chatapp.util.FileUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    private File profilePicture;

    @FXML
    private void handleUpload() {
        profilePicture = FileUtil.chooseFile((Stage) usernameField.getScene().getWindow());
    }

    @FXML
    private void handleRegister() {
        try {
            byte[] profilePictureBytes = profilePicture != null ? FileUtil.imageToBytes(profilePicture) : null;
            if (DatabaseUtil.registerUser(usernameField.getText(), passwordField.getText(), profilePictureBytes)) {
                handleLoginLink();
            } else {
                errorLabel.setText("Registration failed");
            }
        } catch (SQLException | IOException e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleLoginLink() throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/css/light.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("ChatApp - Login");
    }
}