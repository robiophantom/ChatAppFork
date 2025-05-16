package com.chatapp.controller;

import com.chatapp.model.User;
import com.chatapp.util.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        try {
            User user = DatabaseUtil.authenticateUser(usernameField.getText(), passwordField.getText());
            if (user != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(getClass().getResource("/css/light.css").toExternalForm());
                MainController controller = loader.getController();
                controller.setUser(user);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("ChatApp - " + user.getUsername());
            } else {
                errorLabel.setText("Invalid username or password");
            }
        } catch (SQLException | IOException e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegisterLink() throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/css/light.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("ChatApp - Register");
    }
}