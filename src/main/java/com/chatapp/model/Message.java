package com.chatapp.model;

// this is our Message class, a model to represent a chat message in our ChatApp
public class Message {
    // this holds the unique ID of the message, like a label to track it
    private int id;
    // this stores the ID of the user who sent the message
    private int senderId;
    // this keeps the ID of the user who received the message (for direct messages)
    private int recipientId;
    // this holds the group ID if the message is for a group chat (can be null)
    private Integer groupId;
    // this contains the text content of the message, like "Hey, what's up?"
    private String content;
    // this stores the name of any attached file, if there is one
    private String fileName;
    // this holds the actual data of the attached file as bytes
    private byte[] fileData;
    // this tracks the size of the attached file in bytes
    private long fileSize;
    // this records when the message was sent, as a timestamp
    private String sentAt;
    // this flags whether the message has been deleted
    private boolean isDeleted;
    // this stores when the message was delivered, if it has been
    private String deliveredAt;
    // this notes when the message was read, if it has been
    private String readAt;
    // this holds the sender's username, useful for group chats
    private String senderName;

    // this constructor is used when pulling message details from the database
    public Message(int id, int senderId, int recipientId, Integer groupId, String content,
                   String fileName, byte[] fileData, long fileSize, String sentAt,
                   boolean isDeleted, String deliveredAt, String readAt, String senderName) {
        // set the message ID
        this.id = id;
        // assign the sender's ID
        this.senderId = senderId;
        // set the recipient's ID for direct messages
        this.recipientId = recipientId;
        // store the group ID, if this is a group message
        this.groupId = groupId;
        // save the message text content
        this.content = content;
        // record the attached file's name, if any
        this.fileName = fileName;
        // store the file's data as bytes
        this.fileData = fileData;
        // note the file's size
        this.fileSize = fileSize;
        // set the timestamp when the message was sent
        this.sentAt = sentAt;
        // mark whether the message is deleted
        this.isDeleted = isDeleted;
        // save the delivery timestamp, if available
        this.deliveredAt = deliveredAt;
        // store the read timestamp, if available
        this.readAt = readAt;
        // keep the sender's username for group chat display
        this.senderName = senderName;
    }

    // this method lets us get the message's ID
    public int getId() { return id; }
    // this method returns the sender's ID
    public int getSenderId() { return senderId; }
    // this method gives us the recipient's ID
    public int getRecipientId() { return recipientId; }
    // this method returns the group ID, if this is a group message
    public Integer getGroupId() { return groupId; }
    // this method gets the message's text content
    public String getContent() { return content; }
    // this method returns the name of any attached file
    public String getFileName() { return fileName; }
    // this method provides the file data as bytes
    public byte[] getFileData() { return fileData; }
    // this method tells us the file size
    public long getFileSize() { return fileSize; }
    // this method returns when the message was sent
    public String getSentAt() { return sentAt; }
    // this method checks if the message is deleted
    public boolean isDeleted() { return isDeleted; }
    // this method gives us the delivery timestamp
    public String getDeliveredAt() { return deliveredAt; }
    // this method returns when the message was read
    public String getReadAt() { return readAt; }
    // this method provides the sender's username
    public String getSenderName() { return senderName; }

    // this method figures out the message's status (sent, delivered, or read)
    public String getStatus() {
        // if the message is deleted, show no status
        if (isDeleted) return "";
        // if it's been read, show double ticks
        if (readAt != null) return "✓✓"; // read
        // if it's been delivered, show double ticks
        if (deliveredAt != null) return "✓✓"; // delivered
        // otherwise, show a single tick for sent
        return "✓"; // sent
    }
}