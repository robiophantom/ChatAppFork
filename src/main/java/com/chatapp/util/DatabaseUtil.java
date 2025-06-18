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
    private static final String PASSWORD = "qwerasdf";

    // this method gets us a connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // this method signs up a new user by saving their details in the database
    public static boolean registerUser(String username, String password, byte[] profilePicture) throws SQLException {
        String sql = "INSERT INTO users (username, password, profile_picture) VALUES (?, ?, ?)";
        // we use try-with-resources to ensure the connection and statement close automatically, keeping things tidy
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // set the username in the first placeholder (?)
            stmt.setString(1, username);
            // hash the password with BCrypt for security and set it in the second placeholder
            stmt.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            // add the profile picture (as a byte array) to the third placeholder; it can be null if no picture is provided
            stmt.setBytes(3, profilePicture);
            // execute the query and check if at least one row was added; return true if successful otherwise false
            return stmt.executeUpdate() > 0;
        }
    }

    // this method checks if a user can log in by verifying their username and password
    public static User authenticateUser(String username, String password) throws SQLException {
        // sql query to fetch the user's ID, username, hashed password, and profile picture based on the provided username
        String sql = "SELECT id, username, password, profile_picture FROM users WHERE username = ?";
        // try-with-resources keeps our database connection and statement clean
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // plug the username into the query's placeholder
            stmt.setString(1, username);
            // run the query and get the results
            ResultSet rs = stmt.executeQuery();
            // if we find a user and their password matches (using BCrypt to compare), create a User object
            if (rs.next() && BCrypt.checkpw(password, rs.getString("password"))) {
                // return a new User with their ID, username, and profile picture; we're logged in!
                return new User(rs.getInt("id"), rs.getString("username"), rs.getBytes("profile_picture"));
            }
            // if no user is found or the password doesn't match, return null (login failed)
            return null;
        }
    }

    // this method searches for users whose usernames match a query, excluding the current user
    public static List<User> searchUsers(String query, int currentUserId) throws SQLException {
        // we'll store matching users in this list
        List<User> users = new ArrayList<>();
        // sql query to find users where the username contains the query string (case-insensitive) and isn't the current user
        String sql = "SELECT id, username, profile_picture FROM users WHERE username LIKE ? AND id != ?";
        // open a connection and prepare the statement
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // add wildcards (%) to the query for partial matching (e.g., "jo" becomes "%jo%")
            stmt.setString(1, "%" + query + "%");
            // exclude the current user by their ID
            stmt.setInt(2, currentUserId);
            // execute the query and get the results
            ResultSet rs = stmt.executeQuery();
            // loop through each result and create a User object for each matching user
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username"), rs.getBytes("profile_picture")));
            }
        }
        // return the list of matching users (could be empty if no matches)
        return users;
    }

    // this method creates a new group in the database with a given name and creator
    public static boolean createGroup(String name, int createdBy) throws SQLException {
        // sql query to insert a new group into the 'chat_groups' table
        String sql = "INSERT INTO chat_groups (name, created_by) VALUES (?, ?)";
        // set up the connection and statement
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // set the group name in the first placeholder
            stmt.setString(1, name);
            // set the ID of the user who created the group in the second placeholder
            stmt.setInt(2, createdBy);
            // run the query and return true if the group was created successfully
            return stmt.executeUpdate() > 0;
        }
    }

    // this method adds a user to a group by linking them in the 'group_members' table
    public static boolean addGroupMember(int groupId, int userId) throws SQLException {
        // sql query to add a user to a group
        String sql = "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)";
        // open a connection and prepare the statement
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // set the group ID in the first placeholder
            stmt.setInt(1, groupId);
            // set the user ID in the second placeholder
            stmt.setInt(2, userId);
            // execute the query and return true if the member was added
            return stmt.executeUpdate() > 0;
        }
    }

    // this method fetches all groups a user is part of
    public static List<Group> getUserGroups(int userId) throws SQLException {
        // we'll store the user's groups in this list
        List<Group> groups = new ArrayList<>();
        // sql query to get groups where the user is a member, joining 'chat_groups' and 'group_members' tables
        String sql = "SELECT g.id, g.name, g.created_by FROM chat_groups g JOIN group_members gm ON g.id = gm.group_id WHERE gm.user_id = ?";
        // set up the connection and statement
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // set the user ID to find their groups
            stmt.setInt(1, userId);
            // run the query and get the results
            ResultSet rs = stmt.executeQuery();
            // loop through each result and create a Group object
            while (rs.next()) {
                groups.add(new Group(rs.getInt("id"), rs.getString("name"), rs.getInt("created_by")));
            }
        }
        // return the list of groups the user belongs to
        return groups;
    }

    // this method retrieves messages for a direct chat or group chat
    public static List<Message> getMessages(int userId, Integer recipientId, Integer groupId) throws SQLException {
        // we'll store the messages in this list
        List<Message> messages = new ArrayList<>();
        // choose the SQL query based on whether we're fetching group messages or direct messages
        String sql = groupId != null ?
                // for groups, get all messages in the group, including the sender's username
                "SELECT m.*, u.username AS sender_name FROM messages m JOIN users u ON m.sender_id = u.id WHERE m.group_id = ? ORDER BY m.sent_at" :
                // for direct chats, get messages between two users (either direction)
                "SELECT m.* FROM messages m WHERE (m.sender_id = ? AND m.recipient_id = ?) OR (m.sender_id = ? AND m.recipient_id = ?) ORDER BY m.sent_at";
        // open a connection and prepare the statement
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // set the parameters for the query based on whether it's a group or direct chat
            if (groupId != null) {
                // for group messages, just set the group ID
                stmt.setInt(1, groupId);
            } else {
                // for direct messages, set the sender and recipient IDs for both directions
                stmt.setInt(1, userId);
                stmt.setInt(2, recipientId);
                stmt.setInt(3, recipientId);
                stmt.setInt(4, userId);
            }
            // execute the query and get the results
            ResultSet rs = stmt.executeQuery();
            // loop through each message and create a Message object
            while (rs.next()) {
                Message msg = new Message(
                        rs.getInt("id"), // message ID
                        rs.getInt("sender_id"), // who sent it
                        rs.getInt("recipient_id"), // who it was sent to (for direct messages)
                        rs.getInt("group_id"), // group ID (if it's a group message)
                        rs.getBoolean("is_deleted") ? "This message was deleted" : rs.getString("content"), // show deleted message text if applicable
                        rs.getString("file_name"), // name of any attached file
                        rs.getBytes("file_data"), // file data as bytes
                        rs.getLong("file_size"), // size of the file
                        rs.getString("sent_at"), // when it was sent
                        rs.getBoolean("is_deleted"), // is the message deleted?
                        rs.getString("delivered_at"), // when it was delivered
                        rs.getString("read_at"), // when it was read
                        groupId != null ? rs.getString("sender_name") : null // sender's username for group messages
                );
                messages.add(msg);
            }
        }

        // update the status of messages after fetching them
        if (groupId != null) {
            // for group messages, update delivery and read status for the current user
            updateGroupMessageStatus(messages, userId, groupId);
        } else if (recipientId != null && userId != recipientId) {
            // for direct messages, update status if the current user is the recipient
            updateDirectMessageStatus(messages, userId, recipientId);
        }

        // return the list of messages
        return messages;
    }

    // this helper method updates the delivery and read status for direct messages
    private static void updateDirectMessageStatus(List<Message> messages, int userId, int recipientId) throws SQLException {
        // get a database connection
        try (Connection conn = getConnection()) {
            // sql to mark a message as delivered if it hasn't been already
            String updateSql = "UPDATE messages SET delivered_at = NOW() WHERE id = ? AND recipient_id = ? AND delivered_at IS NULL";
            // sql to mark a message as read if it hasn't been already
            String readSql = "UPDATE messages SET read_at = NOW() WHERE id = ? AND recipient_id = ? AND read_at IS NULL";
            // prepare both statements
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                 PreparedStatement readStmt = conn.prepareStatement(readSql)) {
                // loop through each message
                for (Message msg : messages) {
                    // if the current user is the recipient and didn't send the message
                    if (msg.getRecipientId() == userId && msg.getSenderId() != userId) {
                        // mark it as delivered
                        updateStmt.setInt(1, msg.getId());
                        updateStmt.setInt(2, userId);
                        updateStmt.executeUpdate();

                        // also mark it as read
                        readStmt.setInt(1, msg.getId());
                        readStmt.setInt(2, userId);
                        readStmt.executeUpdate();
                    }
                }
            }
        }
    }

    // this helper method updates the delivery and read status for group messages
    private static void updateGroupMessageStatus(List<Message> messages, int userId, int groupId) throws SQLException {
        // get a database connection
        try (Connection conn = getConnection()) {
            // sql to insert or update delivery status for a group message
            String insertSql = "INSERT INTO message_status (message_id, user_id, delivered_at) VALUES (?, ?, NOW()) " +
                    "ON DUPLICATE KEY UPDATE delivered_at = NOW()";
            // sql to mark a group message as read for the current user
            String readSql = "UPDATE message_status SET read_at = NOW() WHERE message_id = ? AND user_id = ? AND read_at IS NULL";
            // prepare both statements
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                 PreparedStatement readStmt = conn.prepareStatement(readSql)) {
                // loop through each message
                for (Message msg : messages) {
                    // if the current user didn't send the message
                    if (msg.getSenderId() != userId) {
                        // mark it as delivered for the current user
                        insertStmt.setInt(1, msg.getId());
                        insertStmt.setInt(2, userId);
                        insertStmt.executeUpdate();

                        // also mark it as read
                        readStmt.setInt(1, msg.getId());
                        readStmt.setInt(2, userId);
                        readStmt.executeUpdate();
                    }
                }
            }
        }
    }

    // this method sends a new message, either to a user or a group
    public static boolean sendMessage(int senderId, Integer recipientId, Integer groupId, String content,
                                      String fileName, byte[] fileData, long fileSize) throws SQLException {
        // sql query to insert a new message into the 'messages' table
        String sql = "INSERT INTO messages (sender_id, recipient_id, group_id, content, file_name, file_data, file_size) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // set up the connection and statement
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // set the sender's ID
            stmt.setInt(1, senderId);
            // set the recipient's ID (null for group messages)
            stmt.setObject(2, recipientId);
            // set the group ID (null for direct messages)
            stmt.setObject(3, groupId);
            // set the message content (text)
            stmt.setString(4, content);
            // set the file name, if any
            stmt.setString(5, fileName);
            // set the file data as bytes, if any
            stmt.setBytes(6, fileData);
            // set the file size
            stmt.setLong(7, fileSize);
            // execute the query and return true if the message was sent
            return stmt.executeUpdate() > 0;
        }
    }

    // this method adds a contact to a user's contact list
    public static boolean addContact(int userId, int contactId) throws SQLException {
        // prevent users from adding themselves as a contact
        if (userId == contactId) return false;
        // sql query to link a user and their contact in the 'contacts' table
        String sql = "INSERT INTO contacts (user_id, contact_id) VALUES (?, ?)";
        // set up the connection and statement
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // set the user's ID
            stmt.setInt(1, userId);
            // set the contact's ID
            stmt.setInt(2, contactId);
            // execute the query and return true if the contact was added
            return stmt.executeUpdate() > 0;
        }
    }

    // this method fetches a user's contact list
    public static List<User> getContacts(int userId) throws SQLException {
        // we'll store the contacts in this list
        List<User> contacts = new ArrayList<>();
        // sql query to get all contacts for a user, joining 'users' and 'contacts' tables
        String sql = "SELECT u.id, u.username, u.profile_picture FROM users u JOIN contacts c ON u.id = c.contact_id WHERE c.user_id = ?";
        // set up the connection and statement
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // set the user's ID to find their contacts
            stmt.setInt(1, userId);
            // execute the query and get the results
            ResultSet rs = stmt.executeQuery();
            // loop through each contact and create a User object
            while (rs.next()) {
                contacts.add(new User(rs.getInt("id"), rs.getString("username"), rs.getBytes("profile_picture")));
            }
        }
        // return the list of contacts
        return contacts;
    }

    // this method deletes a message by marking it as deleted in the database
    public static boolean deleteMessage(int messageId, int senderId) throws SQLException {
        // sql query to mark a message as deleted and clear its content and file data
        String sql = "UPDATE messages SET is_deleted = TRUE, content = NULL, file_name = NULL, file_data = NULL, file_size = 0 WHERE id = ? AND sender_id = ?";
        // set up the connection and statement
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // set the message ID
            stmt.setInt(1, messageId);
            // set the sender's ID to ensure only they can delete their message
            stmt.setInt(2, senderId);
            // execute the query and return true if the message was deleted
            return stmt.executeUpdate() > 0;
        }
    }

    // this method checks the status of a group message (sent, delivered, or read)
    public static String getGroupMessageStatus(int messageId, int senderId, int groupId) throws SQLException {
        // sql query to count group members and check how many have delivered or read the message
        String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN delivered_at IS NOT NULL THEN 1 ELSE 0 END) as delivered, " +
                "SUM(CASE WHEN read_at IS NOT NULL THEN 1 ELSE 0 END) as `read` " +
                "FROM message_status ms JOIN group_members gm ON ms.user_id = gm.user_id " +
                "WHERE ms.message_id = ? AND gm.group_id = ? AND ms.user_id != ?";
        // set up the connection and statement
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // set the message ID
            stmt.setInt(1, messageId);
            // set the group ID
            stmt.setInt(2, groupId);
            // exclude the sender from the count
            stmt.setInt(3, senderId);
            // execute the query and get the results
            ResultSet rs = stmt.executeQuery();
            // if we have results
            if (rs.next()) {
                // get the total number of group members (excluding the sender)
                int total = rs.getInt("total");
                // get how many have received the message
                int delivered = rs.getInt("delivered");
                // get how many have read the message
                int read = rs.getInt("read");
                // if all members have read it, return double ticks (read status)
                if (total > 0 && read == total) return "✓✓";
                // if all members have received it, return double ticks (delivered status)
                if (total > 0 && delivered == total) return "✓✓";
                // otherwise, return a single tick (sent status)
                return "✓";
            }
            // if no status info is available, default to sent
            return "✓";
        }
    }
}