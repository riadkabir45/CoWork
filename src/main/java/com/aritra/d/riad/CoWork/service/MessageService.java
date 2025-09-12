package com.aritra.d.riad.CoWork.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aritra.d.riad.CoWork.dto.ConnectionDTO;
import com.aritra.d.riad.CoWork.dto.MessageDTO;
import com.aritra.d.riad.CoWork.model.Connections;
import com.aritra.d.riad.CoWork.model.Message;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.MessageRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConnectionService connectionService;

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Message getMessageById(String id) {
        return messageRepository.findById(id).orElse(null);
    }

    public Message createMessage(Message message) {
        return messageRepository.save(message);
    }

    public void deleteMessage(String id) {
        messageRepository.deleteById(id);
    }

    public List<Message> getMessagesByConnectionId(String connectionId) {
        return messageRepository.findByConnections(connectionService.getConnectionById(connectionId));
    }

    public List<MessageDTO> getMessagesByConnectionIdDTO(String connectionId) {
        List<Message> messages = getMessagesByConnectionId(connectionId);
        return messages.stream().map(this::generMessageDTO).toList();
    }

    private MessageDTO generMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConnectionsId(message.getConnections().getId());
        dto.setSenderEmail(message.getSender().getEmail());
        dto.setContent(message.getContent());
        dto.setSeen(message.isSeen());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setLastModified(message.getLastModified());
        dto.setEdited(message.isEdited());
        dto.setDeleted(message.isDeleted());
        return dto;
    }

    public List<MessageDTO> getAllMessagesDTO() {
        List<Message> messages = messageRepository.findAll();
        return messages.stream().map(this::generMessageDTO).toList();
    }

    public Message createMessage(String content, ConnectionDTO connectionDTO, Users sender) {
        Connections connections = connectionService.getConnectionById(connectionDTO.getId());
        if (connections.isAccepted()) {
            Message message = new Message();
            message.setContent(content);
            message.setConnections(connections);
            message.setSender(sender);
            return messageRepository.save(message);
        }
        return null;
    }

    public Message createMessage(MessageDTO messageDTO, Users sender) {
        Connections connections = connectionService.getConnectionById(messageDTO.getConnectionsId());
        if (connections.isAccepted()) {
            Message message = new Message();
            message.setContent(messageDTO.getContent());
            message.setConnections(connections);
            message.setSender(sender);
            return messageRepository.save(message);
        }
        return null;
    }

    public MessageDTO createMessageDTO(MessageDTO messageDTO, Users sender) {
        Connections connections = connectionService.getConnectionById(messageDTO.getConnectionsId());
        if (connections.isAccepted()) {
            Message message = new Message();
            message.setContent(messageDTO.getContent());
            message.setConnections(connections);
            message.setSender(sender);
            return generMessageDTO(messageRepository.save(message));
        }
        return null;
    }

    public void markMessagesAsRead(String connectionId, Users authUser) {
        Connections connections = connectionService.getConnectionById(connectionId);
        List<Message> messages = messageRepository.findByConnectionsAndSenderNotAndSeenFalse(connections, authUser);
        for (Message message : messages) {
            message.setSeen(true);
        }
        messageRepository.saveAll(messages);
    }

    public List<Message> getLatestUnreadMessagePerConnection() {
    return messageRepository.findAll().stream()
        .filter(message -> !message.isSeen())
        .collect(Collectors.groupingBy(Message::getConnections,
            Collectors.maxBy(Comparator.comparing(Message::getCreatedAt))))
        .values().stream()
        .flatMap(Optional::stream)
        .collect(Collectors.toList());
    }

    public MessageDTO editMessage(String messageId, String newContent, Users authUser) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Check if the user is the sender of the message
        if (!message.getSender().getId().equals(authUser.getId())) {
            throw new RuntimeException("You can only edit your own messages");
        }
        
        // Check if message is not deleted
        if (message.isDeleted()) {
            throw new RuntimeException("Cannot edit deleted message");
        }
        
        message.setContent(newContent);
        message.setEdited(true);
        message.setLastModified(java.time.LocalDateTime.now());
        
        Message savedMessage = messageRepository.save(message);
        return generMessageDTO(savedMessage);
    }

    public void deleteMessage(String messageId, Users authUser) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Check if the user is the sender of the message
        if (!message.getSender().getId().equals(authUser.getId())) {
            throw new RuntimeException("You can only delete your own messages");
        }
        
        // Soft delete: mark as deleted instead of actually deleting
        message.setDeleted(true);
        message.setContent("This message was deleted");
        message.setLastModified(java.time.LocalDateTime.now());
        
        messageRepository.save(message);
    }

}
