package com.chatapp.controller;

import com.chatapp.model.User;
import com.chatapp.util.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GroupController {
    @FXML private TextField groupNameField;
    @FXML private TextField usernameField;
    @FXML private ListView<String> memberList;
    @FXML private Label errorLabel;
    private MainController mainController;
    private User currentUser;
    private List<User> members = new ArrayList<>();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        members.add(currentUser);
        memberList.getItems().add(currentUser.getUsername());
    }

    @FXML
    private void handleAddMember() {
        try {
            List<User> users = DatabaseUtil.searchUsers(usernameField.getText(), currentUser.getId());
            if (!users.isEmpty()) {
                User user = users.get(0);
                if (!members.contains(user)) {
                    members.add(user);
                    memberList.getItems().add(user.getUsername());
                    usernameField.clear();
                } else {
                    errorLabel.setText("User already added");
                }
            } else {
                errorLabel.setText("User not found");
            }
        } catch (SQLException e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreate() {
        try {
            if (groupNameField.getText().isEmpty()) {
                errorLabel.setText("Group name is required");
                return;
            }
            if (DatabaseUtil.createGroup(groupNameField.getText(), currentUser.getId())) {
                int groupId = getLastGroupId();
                for (User member : members) {
                    DatabaseUtil.addGroupMember(groupId, member.getId());
                }
                mainController.refreshChats();
                ((Stage) groupNameField.getScene().getWindow()).close();
            } else {
                errorLabel.setText("Failed to create group");
            }
        } catch (SQLException e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    private int getLastGroupId() throws SQLException {
        String sql = "SELECT MAX(id) FROM chat_groups";
        try (var conn = DatabaseUtil.getConnection(); var stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }
}