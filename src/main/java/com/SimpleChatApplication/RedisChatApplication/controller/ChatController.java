package com.SimpleChatApplication.RedisChatApplication.controller;

import com.SimpleChatApplication.RedisChatApplication.dto.CreateChatRoomRequest;
import com.SimpleChatApplication.RedisChatApplication.dto.JoinChatRoomRequest;
import com.SimpleChatApplication.RedisChatApplication.dto.MessageRequest;
import com.SimpleChatApplication.RedisChatApplication.models.ChatMessage;
import com.SimpleChatApplication.RedisChatApplication.models.ChatRoom;
import com.SimpleChatApplication.RedisChatApplication.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/room")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody CreateChatRoomRequest request) {
        ChatRoom chatRoom = chatService.createChatRoom(request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(chatRoom);
    }

    @PostMapping("/rooms/join")
    public ResponseEntity<String> joinChatRoom(@RequestBody JoinChatRoomRequest request) {
        chatService.JoinChatRoom(request.getChatRoomId(), request.getUsername());
        return ResponseEntity.ok("Successfully joined chat room");
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessage> sendMessageRest(@RequestBody MessageRequest request) {
        ChatMessage message = chatService.sendMessage(
                request.getChatRoomId(),
                request.getUsername(),
                request.getContent()
        );
        return ResponseEntity.ok(message);
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable String chatRoomId,
            @RequestParam(defaultValue = "50") int limit) {
        List<ChatMessage> messages = chatService.getChatHistory(chatRoomId, limit);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/rooms/{chatRoomId}/participants")
    public ResponseEntity<Set<String>> getChatRoomParticipants(@PathVariable String chatRoomId) {
        Set<String> participants = chatService.getChatRoomParticipants(chatRoomId);
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatService.getAllChatRooms();
        return ResponseEntity.ok(chatRooms);
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessageStomp(@Payload MessageRequest messageRequest) {
        ChatMessage message = chatService.sendMessage(
                messageRequest.getChatRoomId(),
                messageRequest.getUsername(),
                messageRequest.getContent()
        );
        messagingTemplate.convertAndSend("/topic/chatroom/" + messageRequest.getChatRoomId(), message);
    }
}
