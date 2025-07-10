package com.SimpleChatApplication.RedisChatApplication.models;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class ChatRoom {

    private String id;

    private String name;

    private String description;

    private LocalDateTime createdAt;

    private Set<String> participants;

    public ChatRoom(){

    }

    public ChatRoom(String name,String description){
        this.id= UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<String> participants) {
        this.participants = participants;
    }
}
