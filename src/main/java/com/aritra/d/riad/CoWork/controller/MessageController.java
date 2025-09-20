package com.aritra.d.riad.CoWork.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aritra.d.riad.CoWork.dto.MessageDTO;
import com.aritra.d.riad.CoWork.service.MessageService;
import com.aritra.d.riad.CoWork.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/messages")
@Slf4j
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<MessageDTO> getAllMessages() {
        return messageService.getAllMessagesDTO();
    }
    @GetMapping("/{connectionId}")
    public List<MessageDTO> getConnectionMessages(@PathVariable String connectionId) {
        messageService.markMessagesAsRead(connectionId, userService.authUser());
        return messageService.getMessagesByConnectionIdDTO(connectionId);
    }

    @GetMapping("/{connectionId}/status")
    public List<MessageDTO> getConnectionMessagesStatus(@PathVariable String connectionId) {
        // Get messages without marking them as read - for status checking
        return messageService.getMessagesByConnectionIdDTO(connectionId);
    }

    @PostMapping
    public MessageDTO sendMessage(@RequestBody MessageDTO messageDTO) {
        log.info("Received message DTO: {}", messageDTO);
        return messageService.createMessageDTO(messageDTO, userService.authUser());
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageDTO> editMessage(
            @PathVariable String messageId,
            @RequestBody Map<String, String> request) {
        try {
            String newContent = request.get("content");
            if (newContent == null || newContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            MessageDTO editedMessage = messageService.editMessage(messageId, newContent.trim(), userService.authUser());
            return ResponseEntity.ok(editedMessage);
        } catch (RuntimeException e) {
            log.error("Error editing message: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        try {
            messageService.deleteMessage(messageId, userService.authUser());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error deleting message: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
