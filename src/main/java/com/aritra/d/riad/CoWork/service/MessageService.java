package com.aritra.d.riad.CoWork.service;

import java.util.List;

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

}
