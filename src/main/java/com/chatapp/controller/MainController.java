package com.chatapp.controller;

import com.chatapp.model.Group;
import com.chatapp.model.Message;
import com.chatapp.model.User;
import com.chatapp.util.DatabaseUtil;
import com.chatapp.util.FileUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainController {
    @FXML private Label usernameLabel;
    @FXML private ImageView profileImage;
    @FXML private CheckBox themeToggle;
    @FXML private TextField searchField;
    @FXML private ListView<String> chatList;
    @FXML private Label chatTitle;
    @FXML private VBox messageContainer;
    @FXML private ScrollPane messageScrollPane;
    @FXML private TextArea messageField;
    @FXML private Button addContactButton;
    private User currentUser;
    private User selectedUser;
    private Group selectedGroup;
    private boolean isDarkMode = false;
    private boolean shouldScrollToBottom = true; // Flag to control scrolling behavior

    public void setUser(User user) {
        this.currentUser = user;
        usernameLabel.setText(user.getUsername());
        profileImage.setImage(FileUtil.bytesToImage(user.getProfilePicture()));
        loadChats();
        startMessagePolling();
    }

    private void loadChats() {
        try {
            chatList.getItems().clear();
            List<User> contacts = DatabaseUtil.getContacts(currentUser.getId());
            for (User contact : contacts) {
                chatList.getItems().add("Contact: " + contact.getUsername());
            }
            List<Group> groups = DatabaseUtil.getUserGroups(currentUser.getId());
            for (Group group : groups) {
                chatList.getItems().add("Group: " + group.getName());
            }
            if (chatList.getItems().isEmpty()) {
                chatList.getItems().add("No contacts or groups");
            }
            chatList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.equals("No contacts or groups")) {
                    if (newVal.startsWith("Contact: ")) {
                        selectedGroup = null;
                        String username = newVal.replace("Contact: ", "");
                        selectedUser = contacts.stream()
                                .filter(u -> u.getUsername().equals(username))
                                .findFirst()
                                .orElse(null);
                    } else if (newVal.startsWith("Group: ")) {
                        selectedUser = null;
                        selectedGroup = groups.stream()
                                .filter(g -> ("Group: " + g.getName()).equals(newVal))
                                .findFirst()
                                .orElse(null);
                    }
                    chatTitle.setText(newVal);
                    shouldScrollToBottom = true; // Scroll to bottom when a new chat is selected
                    loadMessages();
                }
            });
        } catch (SQLException e) {
            showAlert("Error loading chats: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        try {
            String query = searchField.getText();
            List<User> users = DatabaseUtil.searchUsers(query, currentUser.getId());
            chatList.getItems().clear();
            for (User user : users) {
                chatList.getItems().add(user.getUsername());
            }
            if (users.isEmpty()) {
                chatList.getItems().add("No users found");
            }
            chatList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.equals("No users found")) {
                    selectedGroup = null;
                    selectedUser = users.stream()
                            .filter(u -> u.getUsername().equals(newVal))
                            .findFirst()
                            .orElse(null);
                    chatTitle.setText(newVal);
                    shouldScrollToBottom = true; // Scroll to bottom when a new chat is selected
                    loadMessages();
                    addContactButton.setVisible(true);
                } else {
                    addContactButton.setVisible(false);
                }
            });
        } catch (SQLException e) {
            showAlert("Error searching users: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddContact() {
        if (selectedUser != null) {
            try {
                if (DatabaseUtil.addContact(currentUser.getId(), selectedUser.getId())) {
                    showAlert("Contact added successfully");
                    loadChats();
                    addContactButton.setVisible(false);
                } else {
                    showAlert("Failed to add contact");
                }
            } catch (SQLException e) {
                showAlert("Error adding contact: " + e.getMessage());
            }
        }
    }

    private void loadMessages() {
        try {
            // Capture the current scroll position before reloading messages
            double currentScrollPos = messageScrollPane.getVvalue();
            boolean wasAtBottom = currentScrollPos >= 0.99; // Consider "at bottom" if very close to 1.0

            messageContainer.getChildren().clear();
            List<Message> messages = DatabaseUtil.getMessages(
                    currentUser.getId(),
                    selectedUser != null ? selectedUser.getId() : null,
                    selectedGroup != null ? selectedGroup.getId() : null
            );
            for (Message msg : messages) {
                // Outer HBox to align the entire message bubble (left for sender, right for user)
                HBox messageWrapper = new HBox();
                messageWrapper.setAlignment(msg.getSenderId() == currentUser.getId() ?
                        javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
                HBox.setHgrow(messageWrapper, Priority.ALWAYS);

                // VBox for the bubble itself, containing both message content and metadata
                VBox messageBubble = new VBox();
                messageBubble.getStyleClass().add("message-bubble");
                if (msg.getSenderId() == currentUser.getId()) {
                    messageBubble.getStyleClass().add("message-bubble-sent");
                }
                VBox.setVgrow(messageBubble, Priority.ALWAYS);

                // Message content
                Label content = new Label();
                if (msg.isDeleted()) {
                    content.setText("This message was deleted");
                    content.getStyleClass().add("message-deleted");
                } else if (msg.getContent() != null) {
                    content.setText(msg.getContent());
                } else if (msg.getFileName() != null) {
                    content.setText("File: " + msg.getFileName());
                    content.setOnMouseClicked(e -> {
                        try {
                            FileUtil.saveFile(msg.getFileData(), msg.getFileName());
                        } catch (IOException ex) {
                            showAlert("Error saving file: " + ex.getMessage());
                        }
                    });
                }
                content.setWrapText(true);
                content.setPrefWidth(Region.USE_COMPUTED_SIZE);
                content.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(content, Priority.ALWAYS);

                // HBox for metadata (timestamp and status)
                HBox metaBox = new HBox(5);
                metaBox.setAlignment(javafx.geometry.Pos.BOTTOM_RIGHT);
                HBox.setHgrow(metaBox, Priority.ALWAYS);
                Label timestamp = new Label(formatTimestamp(msg.getSentAt()));
                timestamp.getStyleClass().add("message-meta");
                Label status = new Label();
                if (msg.getSenderId() == currentUser.getId() && !msg.isDeleted()) {
                    String statusText = selectedGroup != null ?
                            DatabaseUtil.getGroupMessageStatus(msg.getId(), currentUser.getId(), selectedGroup.getId()) :
                            msg.getStatus();
                    status.setText(statusText);
                    status.getStyleClass().add("message-status");
                    if (statusText.equals("✓✓ (blue)")) {
                        status.setStyle("-fx-text-fill: #4fc3f7;"); // Blue for read
                    }
                }
                metaBox.getChildren().addAll(timestamp, status);

                // Add content and metadata to the bubble
                messageBubble.getChildren().addAll(content, metaBox);
                messageWrapper.getChildren().add(messageBubble);

                // Add context menu for sender's messages
                if (msg.getSenderId() == currentUser.getId() && !msg.isDeleted()) {
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem deleteItem = new MenuItem("Delete Message");
                    deleteItem.setOnAction(e -> {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this message?", ButtonType.YES, ButtonType.NO);
                        confirm.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.YES) {
                                try {
                                    if (DatabaseUtil.deleteMessage(msg.getId(), currentUser.getId())) {
                                        loadMessages();
                                    } else {
                                        showAlert("Failed to delete message");
                                    }
                                } catch (SQLException ex) {
                                    showAlert("Error deleting message: " + ex.getMessage());
                                }
                            }
                        });
                    });
                    contextMenu.getItems().add(deleteItem);
                    messageBubble.setOnContextMenuRequested(e -> {
                        contextMenu.show(messageBubble, e.getScreenX(), e.getScreenY());
                    });
                }

                messageContainer.getChildren().add(messageWrapper);
            }

            // Apply CSS and layout
            messageScrollPane.applyCss();
            messageScrollPane.layout();

            // Scroll logic
            if (shouldScrollToBottom || wasAtBottom) {
                messageScrollPane.setVvalue(1.0); // Scroll to bottom
                shouldScrollToBottom = false; // Reset flag after scrolling
            } else {
                messageScrollPane.setVvalue(currentScrollPos); // Preserve scroll position
            }
        } catch (SQLException e) {
            showAlert("Error loading messages: " + e.getMessage());
        }
    }

    @FXML
    private void handleSend() {
        try {
            String content = messageField.getText();
            if (!content.isEmpty()) {
                DatabaseUtil.sendMessage(
                        currentUser.getId(),
                        selectedUser != null ? selectedUser.getId() : null,
                        selectedGroup != null ? selectedGroup.getId() : null,
                        content,
                        null,
                        null,
                        0
                );
                messageField.clear();
                shouldScrollToBottom = true; // Scroll to bottom after sending a message
                loadMessages();
            }
        } catch (SQLException e) {
            showAlert("Error sending message: " + e.getMessage());
        }
    }

    @FXML
    private void handleAttach() {
        File file = FileUtil.chooseFile((Stage) messageField.getScene().getWindow());
        if (file != null) {
            try {
                byte[] fileData = FileUtil.fileToBytes(file);
                DatabaseUtil.sendMessage(
                        currentUser.getId(),
                        selectedUser != null ? selectedUser.getId() : null,
                        selectedGroup != null ? selectedGroup.getId() : null,
                        null,
                        file.getName(),
                        fileData,
                        file.length()
                );
                shouldScrollToBottom = true; // Scroll to bottom after attaching a file
                loadMessages();
            } catch (IOException | SQLException e) {
                showAlert("Error attaching file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCreateGroup() throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/group.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource(isDarkMode ? "/css/dark.css" : "/css/light.css").toExternalForm());
        GroupController controller = loader.getController();
        controller.setMainController(this);
        controller.setCurrentUser(currentUser);
        stage.setScene(scene);
        stage.setTitle("Create Group");
        stage.show();
    }

    @FXML
    private void toggleTheme() {
        isDarkMode = themeToggle.isSelected();
        Scene scene = usernameLabel.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource(isDarkMode ? "/css/dark.css" : "/css/light.css").toExternalForm());
    }

    @FXML
    private void handleLogout() throws IOException {
        Stage stage = (Stage) usernameLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/css/light.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("ChatApp - Login");
    }

    private void startMessagePolling() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (selectedUser != null || selectedGroup != null) {
                        loadMessages();
                    }
                });
            }
        }, 0, 2000);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refreshChats() {
        loadChats();
    }

    private String formatTimestamp(String sentAt) {
        // Assuming sentAt is in format "yyyy-MM-dd HH:mm:ss"
        try {
            String[] parts = sentAt.split(" ")[1].split(":");
            int hour = Integer.parseInt(parts[0]);
            String minute = parts[1];
            String period = hour >= 12 ? "PM" : "AM";
            hour = hour % 12 == 0 ? 12 : hour % 12;
            return String.format("%d:%s %s", hour, minute, period);
        } catch (Exception e) {
            return sentAt; // Fallback
        }
    }
}