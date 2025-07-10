package com.SimpleChatApplication.RedisChatApplication.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChatMessage {


    private String id;

    private String chatRoomId;

    private String username;

    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String chatRoomId, String username, String content){
        this.id = UUID.randomUUID().toString();
        this.chatRoomId=chatRoomId;
        this.username=username;
        this.content=content;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
