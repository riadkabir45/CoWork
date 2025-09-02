package com.aritra.d.riad.CoWork.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
        return messageService.getMessagesByConnectionIdDTO(connectionId);
    }

    @PostMapping
    public MessageDTO sendMessage(@RequestBody MessageDTO messageDTO) {
        log.info("Received message DTO: {}", messageDTO);
        return messageService.createMessageDTO(messageDTO, userService.authUser());
    }
}
