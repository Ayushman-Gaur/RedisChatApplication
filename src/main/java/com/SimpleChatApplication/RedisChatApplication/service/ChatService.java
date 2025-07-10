package com.SimpleChatApplication.RedisChatApplication.service;

import com.SimpleChatApplication.RedisChatApplication.exception.ChatRoomAlreadyExistsException;
import com.SimpleChatApplication.RedisChatApplication.exception.ChatRoomNotFoundException;
import com.SimpleChatApplication.RedisChatApplication.models.ChatMessage;
import com.SimpleChatApplication.RedisChatApplication.models.ChatRoom;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ChatService {
    private static final String CHAT_ROOM_PREFIX="chatroom:";
    private static final String CHAT_ROOM_MESSAGE_PREFIX="chatroom:message:";
    private static final String CHAT_ROOM_PARTICIPANTS_PREFIX="chatroom:participants:";
    private static final String CHAT_ROOM_NAMES_SET="chatroom:names";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;
    @Autowired
    private ObjectMapper objectMapper;

    public ChatRoom createChatRoom(String name, String description){
        if(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(CHAT_ROOM_NAMES_SET, name))){
            throw new ChatRoomAlreadyExistsException("Chat room with name "+ name+" already exists");
        }

        ChatRoom chatRoom = new ChatRoom(name, description);

        redisTemplate.opsForHash().putAll(CHAT_ROOM_PREFIX+chatRoom.getId(),
                objectMapper.convertValue(chatRoom,java.util.Map.class));

        redisTemplate.opsForSet().add(CHAT_ROOM_NAMES_SET,name);

        subscribeToChannel(chatRoom.getId());

        return chatRoom;
    }

    public void JoinChatRoom(String chatRoomId, String username){
        if(!redisTemplate.hasKey(CHAT_ROOM_PREFIX+chatRoomId)){
            throw new ChatRoomNotFoundException("Chat room with id "+ chatRoomId+" not found");
        }

        redisTemplate.opsForSet().add(CHAT_ROOM_PARTICIPANTS_PREFIX+chatRoomId,username);
    }

    public ChatMessage sendMessage(String chatRoomId, String username, String content){
        if(!redisTemplate.hasKey(CHAT_ROOM_PREFIX+chatRoomId)){
            throw new ChatRoomNotFoundException("Chat room with id "+ chatRoomId+" not found");
        }

        ChatMessage message= new ChatMessage(chatRoomId,username,content);

        try{
            String messageJson= objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().leftPush(CHAT_ROOM_MESSAGE_PREFIX+chatRoomId,messageJson);
            redisTemplate.convertAndSend("chatroom: "+ chatRoomId,messageJson);

            return message;
        }catch (JsonProcessingException e){
                throw new RuntimeException("Error serializing message",e);
        }
    }


    public List<ChatMessage> getChatHistory(String chatRoomId, int limit){
        if(!redisTemplate.hasKey(CHAT_ROOM_PREFIX+chatRoomId)){
            throw new ChatRoomNotFoundException("Chat room with id "+ chatRoomId+" not found");
        }
        List<Object> message=redisTemplate.opsForList().range(
                CHAT_ROOM_MESSAGE_PREFIX+chatRoomId,0,limit-1
        );

        assert message != null;
        return message.stream()
                .map(this::deserializeMessage)
                .collect(Collectors.toList());
    }

    public Set<String> getChatRoomParticipants(String chatRoomId){
        if(!redisTemplate.hasKey(CHAT_ROOM_PREFIX+chatRoomId)){
            throw new ChatRoomNotFoundException("Chat room with id "+ chatRoomId+" not found");
        }
        return redisTemplate.opsForSet().members(CHAT_ROOM_PARTICIPANTS_PREFIX+chatRoomId)
                .stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    public List<ChatRoom> getAllChatRooms() {
        Set<String> keys = redisTemplate.keys(CHAT_ROOM_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        return keys.stream()
                .map(this::getChatRoomByKey)
                .filter(chatRoom -> chatRoom != null)
                .collect(Collectors.toList());
    }

    private ChatRoom getChatRoomByKey(String key) {
        try {
            Map<Object, Object> roomData = redisTemplate.opsForHash().entries(key);
            if (roomData.isEmpty()) {
                return null;
            }

            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setId(roomData.get("id") != null ? roomData.get("id").toString() : null);
            chatRoom.setName(roomData.get("name") != null ? roomData.get("name").toString() : null);
            chatRoom.setDescription(roomData.get("description") != null ? roomData.get("description").toString() : null);

            // Handle createdAt field
            if (roomData.get("createdAt") != null) {
                String createdAtStr = roomData.get("createdAt").toString();
                try {
                    chatRoom.setCreatedAt(java.time.LocalDateTime.parse(createdAtStr));
                } catch (Exception e) {
                    chatRoom.setCreatedAt(java.time.LocalDateTime.now());
                }
            }

            return chatRoom;
        } catch (Exception e) {
            System.err.println("Error deserializing chat room from key: " + key + ", error: " + e.getMessage());
            return null;
        }
    }

    private ChatRoom getChatRoomByName(String name){
        Set<String> keys= redisTemplate.keys(CHAT_ROOM_PREFIX+ "*");
        for(String key:keys){
            Map<Object,Object> roomData=redisTemplate.opsForHash().entries(key);
            if(name.equals(roomData.get("name"))){
                return objectMapper.convertValue(roomData,ChatRoom.class);
            }
        }
        return null;
    }


    private ChatMessage deserializeMessage(Object message){
        try {
            return objectMapper.readValue(message.toString(),ChatMessage.class);
        }catch (JsonProcessingException e){
            throw new RuntimeException("Error serializing message",e);
        }
    }

    private void subscribeToChannel(String chatRoomId){
        ChannelTopic topic =new ChannelTopic("chatroom:"+chatRoomId);
        redisMessageListenerContainer.addMessageListener((message,pattern)->{
            try {
                ChatMessage chatMessage = objectMapper.readValue(message.getBody(), ChatMessage.class);
                messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId, chatMessage);
            } catch (IOException e) {
                // Log error
                System.err.println("Error processing message: " + e.getMessage());
            }
        },topic);
    }

}
