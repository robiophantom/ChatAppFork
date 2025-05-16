package com.chatapp.util;

import com.chatapp.model.Group;
import com.chatapp.model.Message;
import com.chatapp.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/chatapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "rawal117";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static User authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, profile_picture FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && BCrypt.checkpw(password, rs.getString("password"))) {
                return new User(rs.getInt("id"), rs.getString("username"), rs.getBytes("profile_picture"));
            }
            return null;
        }
    }

    public static boolean registerUser(String username, String password, byte[] profilePicture) throws SQLException {
        String sql = "INSERT INTO users (username, password, profile_picture) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            stmt.setBytes(3, profilePicture);
            return stmt.executeUpdate() > 0;
        }
    }

    public static List<User> searchUsers(String query, int currentUserId) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, profile_picture FROM users WHERE username LIKE ? AND id != ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            stmt.setInt(2, currentUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username"), rs.getBytes("profile_picture")));
            }
        }
        return users;
    }

    public static boolean createGroup(String name, int createdBy) throws SQLException {
        String sql = "INSERT INTO chat_groups (name, created_by) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, createdBy);
            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean addGroupMember(int groupId, int userId) throws SQLException {
        String sql = "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static List<Group> getUserGroups(int userId) throws SQLException {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.id, g.name, g.created_by FROM chat_groups g JOIN group_members gm ON g.id = gm.group_id WHERE gm.user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                groups.add(new Group(rs.getInt("id"), rs.getString("name"), rs.getInt("created_by")));
            }
        }
        return groups;
    }

    public static List<Message> getMessages(int userId, Integer recipientId, Integer groupId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = groupId != null ?
                "SELECT * FROM messages WHERE group_id = ? ORDER BY sent_at" :
                "SELECT * FROM messages WHERE (sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?) ORDER BY sent_at";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (groupId != null) {
                stmt.setInt(1, groupId);
            } else {
                stmt.setInt(1, userId);
                stmt.setInt(2, recipientId);
                stmt.setInt(3, recipientId);
                stmt.setInt(4, userId);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                        rs.getInt("id"),
                        rs.getInt("sender_id"),
                        rs.getInt("recipient_id"),
                        rs.getInt("group_id"),
                        rs.getString("content"),
                        rs.getString("file_name"),
                        rs.getBytes("file_data"),
                        rs.getLong("file_size"),
                        rs.getString("sent_at")
                ));
            }
        }
        return messages;
    }

    public static boolean sendMessage(int senderId, Integer recipientId, Integer groupId, String content,
                                      String fileName, byte[] fileData, long fileSize) throws SQLException {
        String sql = "INSERT INTO messages (sender_id, recipient_id, group_id, content, file_name, file_data, file_size) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setObject(2, recipientId);
            stmt.setObject(3, groupId);
            stmt.setString(4, content);
            stmt.setString(5, fileName);
            stmt.setBytes(6, fileData);
            stmt.setLong(7, fileSize);
            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean addContact(int userId, int contactId) throws SQLException {
        if (userId == contactId) return false; // Prevent adding self
        String sql = "INSERT INTO contacts (user_id, contact_id) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, contactId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static List<User> getContacts(int userId) throws SQLException {
        List<User> contacts = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.profile_picture FROM users u JOIN contacts c ON u.id = c.contact_id WHERE c.user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                contacts.add(new User(rs.getInt("id"), rs.getString("username"), rs.getBytes("profile_picture")));
            }
        }
        return contacts;
    }
}