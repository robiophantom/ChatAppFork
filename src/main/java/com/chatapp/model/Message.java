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
}