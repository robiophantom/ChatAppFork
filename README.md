ChatApp
ChatApp is a JavaFX-based desktop messaging application that allows users to communicate via one-on-one chats and group conversations, similar to WhatsApp. It features user authentication, contact management, file sharing, and a light/dark theme toggle. The application uses a MySQL database for persistent storage and supports real-time message updates through polling.
Features

User Authentication: Register and log in with a username, password, and optional profile picture.
Contact Management: Add users as contacts to maintain a personalized chat list.
One-on-One Messaging: Send text messages and files (up to 15 MB) to contacts.
Group Chats: Create groups, add members, and send messages or files to the group.
File Sharing: Attach and download files within chats, with clickable file links.
Theme Support: Toggle between light and dark themes for a customizable UI.
Real-Time Updates: Messages are refreshed every 2 seconds via polling.
Search Functionality: Search for users to start chats or add them as contacts.

Project Structure
ChatApp/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/
│   │   │   │   ├── chatapp/
│   │   │   │   │   ├── controller/           # UI logic controllers
│   │   │   │   │   │   ├── LoginController.java
│   │   │   │   │   │   ├── RegisterController.java
│   │   │   │   │   │   ├── MainController.java
│   │   │   │   │   │   ├── GroupController.java
│   │   │   │   │   ├── model/                # Data models
│   │   │   │   │   │   ├── User.java
│   │   │   │   │   │   ├── Message.java
│   │   │   │   │   │   ├── Group.java
│   │   │   │   │   ├── util/                 # Utility classes
│   │   │   │   │   │   ├── DatabaseUtil.java
│   │   │   │   │   │   ├── FileUtil.java
│   │   │   │   │   ├── App.java              # Main application entry point
│   │   ├── resources/
│   │   │   ├── css/                          # Light and dark theme styles
│   │   │   │   ├── light.css
│   │   │   │   ├── dark.css
│   │   │   ├── fxml/                         # UI layouts
│   │   │   │   ├── login.fxml
│   │   │   │   ├── register.fxml
│   │   │   │   ├── main.fxml
│   │   │   │   ├── group.fxml
│   │   │   ├── images/                       # Default profile image
│   │   │   │   ├── default_profile.png
├── pom.xml                                   # Maven configuration
├── README.md                                 # This file

Technologies Used

Java 21: Core programming language.
JavaFX 21: For the graphical user interface.
MySQL: Backend database for storing users, contacts, groups, and messages.
Maven: Dependency management and build tool.
BCrypt: Password hashing for secure authentication.
MySQL Connector: JDBC driver for database connectivity.

Prerequisites

Java Development Kit (JDK) 21: Ensure JDK 21 is installed.
Maven: For building and managing dependencies.
MySQL Server: Version 8.0 or higher, with a database named chatapp.
IDE: IntelliJ IDEA, Eclipse, or any IDE with JavaFX support (optional but recommended).

Setup Instructions

Clone the Repository:
git clone <repository-url>
cd ChatApp


Configure MySQL:

Install MySQL Server and ensure it’s running.
Create a database named chatapp:CREATE DATABASE chatapp;


Run the following SQL script to set up the schema (available in the project documentation or below):USE chatapp;

-- Drop tables in reverse order to avoid foreign key issues
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS group_members;
DROP TABLE IF EXISTS contacts;
DROP TABLE IF EXISTS chat_groups;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    profile_picture LONGBLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Create chat_groups table
CREATE TABLE chat_groups (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

-- Create group_members table
CREATE TABLE group_members (
    group_id INT,
    user_id INT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES chat_groups(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Create contacts table
CREATE TABLE contacts (
    user_id INT,
    contact_id INT,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, contact_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (contact_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Create messages table
CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT,
    recipient_id INT,
    group_id INT,
    content TEXT,
    file_name VARCHAR(255),
    file_data LONGBLOB,
    file_size BIGINT,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (recipient_id) REFERENCES users(id),
    FOREIGN KEY (group_id) REFERENCES chat_groups(id)
) ENGINE=InnoDB;




Update Database Credentials:

Open src/main/java/com/chatapp/util/DatabaseUtil.java.
Update the URL, USER, and PASSWORD fields to match your MySQL configuration:private static final String URL = "jdbc:mysql://localhost:3306/chatapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String USER = "root";
private static final String PASSWORD = "your_password";




Build the Project:

Run the following Maven command to download dependencies and build the project:mvn clean install




Run the Application:

Execute the main class com.chatapp.App:mvn javafx:run


Alternatively, run it from your IDE by setting the main class to com.chatapp.App.



How It Works
1. User Authentication

Login: Users enter a username and password. The LoginController uses DatabaseUtil.authenticateUser to verify credentials against the users table, using BCrypt for password comparison. On success, the app loads the main chat window.
Register: Users provide a username, password, and optional profile picture. The RegisterController hashes the password with BCrypt and stores the user in the users table via DatabaseUtil.registerUser. After registration, users are redirected to the login screen.

2. Contact Management

Users can search for other users by username using the search bar in the main window (MainController.handleSearch).
Selecting a user displays an “Add Contact” button. Clicking it adds the user to the contacts table via DatabaseUtil.addContact.
Contacts appear in the chat list with a “Contact: ” prefix, alongside groups (prefixed with “Group: ”).
Selecting a contact loads the message history (sent and received) using DatabaseUtil.getMessages.

3. Messaging

One-on-One Chats: Users select a contact from the chat list to start a conversation. Messages are stored in the messages table with sender_id and recipient_id. The MainController displays messages in a scrollable pane, with sent messages right-aligned and purple, and received messages left-aligned and light purple (or grey in dark mode).
Group Chats: Users select a group to view group messages, stored with a group_id in the messages table.
File Sharing: Users can attach files (up to 15 MB) using the “Attach” button. Files are stored as LONGBLOB in the messages table and can be downloaded by clicking the file name in the chat.

4. Group Creation

Users click “Create Group” in the main window to open the group creation UI (GroupController).
Enter a group name and add members by searching usernames. The GroupController uses DatabaseUtil.searchUsers to find users, excluding the current user.
On creation, the group is added to the chat_groups table, and members are added to the group_members table. The chat list refreshes to show the new group.

5. Real-Time Updates

The MainController polls the database every 2 seconds (startMessagePolling) to refresh messages for the selected contact or group, ensuring new messages appear without manual refresh.

6. Theming

Users can toggle between light and dark themes using a checkbox in the main window. The MainController.toggleTheme method switches between light.css and dark.css, updating the UI styling.

Usage

Start the App:

Run the application using mvn javafx:run or your IDE.
The login window appears.


Register a User:

Click the “Register” link, enter a username, password, and optional profile picture, then click “Register”.
Return to the login screen.


Log In:

Enter your username and password to access the main chat window.


Add Contacts:

In the search bar, type a username (e.g., testuser) and press Enter.
Select the user from the chat list and click “Add Contact”.
The contact appears in the chat list with “Contact: ” prefix.


Start Chatting:

Select a contact or group from the chat list.
Type a message and click “Send”, or attach a file using “Attach”.
Click file names in the chat to download them.


Create a Group:

Click “Create Group”, enter a group name, and add members by searching usernames.
Click “Create” to add the group to the chat list.


Toggle Theme:

Check/uncheck the “Dark Mode” checkbox to switch themes.



Troubleshooting

Database Connection Error:

Ensure MySQL is running and the credentials in DatabaseUtil.java are correct.
Verify the JDBC URL includes useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true.


No Users Found in Search:

Check the users table has entries:SELECT * FROM users;


Insert test users if needed:INSERT INTO users (username, password) VALUES ('testuser', '$2a$10$...');




Group Creation Fails:

Ensure the searchUsers method is called with the correct parameters (username and current user ID).
Check the chat_groups and group_members tables for data.


Messages Not Updating:

Verify the polling timer in MainController.startMessagePolling is running.
Check the messages table for new entries.



Future Improvements

WebSocket Support: Replace polling with WebSockets for real-time messaging.
Message Previews: Show the last message in the chat list for contacts and groups.
Online Status: Indicate when contacts are online.
Read Receipts: Add indicators for read/unread messages.
File Storage Optimization: Store files on a file system or cloud storage instead of the database.

Contributing
Contributions are welcome! Please fork the repository, create a new branch, and submit a pull request with your changes. Ensure your code follows the existing style and includes tests where applicable.
