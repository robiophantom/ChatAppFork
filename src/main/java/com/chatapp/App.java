package com.chatapp;

// let's bring in JavaFX tools to build our app's user interface
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

// we need this to handle potential file loading errors
import java.io.IOException;

// this is our App class, the starting point for our ChatApp
public class App extends Application {
    // this method is where our JavaFX app kicks off, setting up the main window
    @Override
    public void start(Stage stage) throws IOException {
        // load the login screen layout from the login.fxml file
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));
        // create a scene with the loaded layout, setting its size to 800x600 pixels
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        // add our dark theme stylesheet to style the login screen
        scene.getStylesheets().add(getClass().getResource("/css/dark.css").toExternalForm());
        // set the window title to "ChatApp - Login"
        stage.setTitle("ChatApp - Login");
        // attach the scene to the main window
        stage.setScene(scene);
        // show the window to the user
        stage.show();
    }

    // this is the main method, the entry point when we run the app
    public static void main(String[] args) {
        // launch the JavaFX application, passing any command-line arguments
        launch(args);
    }
}