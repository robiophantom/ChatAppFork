package com.chatapp.model;

public class Message {
    private int id;
    private int senderId;
    private int recipientId;
    private Integer groupId;
    private String content;
    private String fileName;
    private byte[] fileData;
    private long fileSize;
    private String sentAt;
    private boolean isDeleted;
    private String deliveredAt;
    private String readAt;

    public Message(int id, int senderId, int recipientId, Integer groupId, String content,
                   String fileName, byte[] fileData, long fileSize, String sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.groupId = groupId;
        this.content = content;
        this.fileName = fileName;
        this.fileData = fileData;
        this.fileSize = fileSize;
        this.sentAt = sentAt;
        this.isDeleted = false;
    }

    // Constructor for database retrieval
    public Message(int id, int senderId, int recipientId, Integer groupId, String content,
                   String fileName, byte[] fileData, long fileSize, String sentAt,
                   boolean isDeleted, String deliveredAt, String readAt) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.groupId = groupId;
        this.content = content;
        this.fileName = fileName;
        this.fileData = fileData;
        this.fileSize = fileSize;
        this.sentAt = sentAt;
        this.isDeleted = isDeleted;
        this.deliveredAt = deliveredAt;
        this.readAt = readAt;
    }

    // Getters
    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public int getRecipientId() { return recipientId; }
    public Integer getGroupId() { return groupId; }
    public String getContent() { return content; }
    public String getFileName() { return fileName; }
    public byte[] getFileData() { return fileData; }
    public long getFileSize() { return fileSize; }
    public String getSentAt() { return sentAt; }
    public boolean isDeleted() { return isDeleted; }
    public String getDeliveredAt() { return deliveredAt; }
    public String getReadAt() { return readAt; }

    // Determine message status
    public String getStatus() {
        if (isDeleted) return "";
        if (readAt != null) return "✓✓ (blue)"; // Read
        if (deliveredAt != null) return "✓✓"; // Delivered
        return "✓"; // Sent
    }
}