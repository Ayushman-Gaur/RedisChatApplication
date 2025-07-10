package com.SimpleChatApplication.RedisChatApplication.dto;

public class JoinChatRoomRequest {

    private String chatRoomId;

    private String username;

    public JoinChatRoomRequest() {}

    public JoinChatRoomRequest(String chatRoomId, String username) {
        this.chatRoomId = chatRoomId;
        this.username = username;
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
}
