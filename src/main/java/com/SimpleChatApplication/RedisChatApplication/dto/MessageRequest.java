package com.SimpleChatApplication.RedisChatApplication.dto;

public class MessageRequest {

    private String chatRoomId;

    private String username;

    private String content;


    public MessageRequest(){

    }

    public MessageRequest(String chatRoomId, String username, String content){
        this.chatRoomId = chatRoomId;
        this.username = username;
        this.content = content;
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
}
