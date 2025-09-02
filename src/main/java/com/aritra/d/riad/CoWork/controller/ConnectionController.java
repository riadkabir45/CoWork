package com.aritra.d.riad.CoWork.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aritra.d.riad.CoWork.dto.ConnectionDTO;
import com.aritra.d.riad.CoWork.dto.SimpleUserDTO;
import com.aritra.d.riad.CoWork.model.Connections;
import com.aritra.d.riad.CoWork.service.ConnectionService;
import com.aritra.d.riad.CoWork.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/connections")
@Slf4j
public class ConnectionController {

    @Autowired
    private UserService userService;

    @Autowired
    private ConnectionService connectionService;

    @GetMapping
    public List<ConnectionDTO> getAllConnections() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authUserEmail = authentication.getName();
        return connectionService.getUserConnectionsDTO(userService.findByEmail(authUserEmail).orElseThrow());
    }

    @GetMapping("/connected")
    public List<SimpleUserDTO> getAllConnectedUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authUserEmail = authentication.getName();
        return userService.generateSimpleUserDTO(connectionService.getAllConnectedUsers(userService.findByEmail(authUserEmail).orElseThrow()));
    }

    @GetMapping("/accepted")
    public List<ConnectionDTO> getAllAcceptedConnections() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authUserEmail = authentication.getName();
        return connectionService.getUserAcceptedConnectionsDTO(userService.findByEmail(authUserEmail).orElseThrow());
    }

    @PostMapping
    public ResponseEntity<String> acceptConnection(@RequestBody ConnectionDTO connectionDTO) {
        Connections connection = connectionService.getConnectionById(connectionDTO.getId());

        log.info("Accepting connection: {}", connection);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authUserEmail = authentication.getName();

        var user = userService.findByEmail(authUserEmail);

        if(user.isPresent()) {
            if(connectionService.checkConnectionRecv(user.get())) {
                connectionService.acceptConnection(connection);
                return ResponseEntity.ok().body("Connection accepted");
            }
        }
        return ResponseEntity.ok().body("Warn: Invalid connection request");
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<ConnectionDTO> getConnectionDetails(@PathVariable String id) {
        Connections connection = connectionService.getConnectionById(id);
        if (connection != null) {
            return ResponseEntity.ok().body(connectionService.generateConnectionsDTO(connection));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{email}")
    public ResponseEntity<String> createConnection(@PathVariable String email) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authUserEmail = authentication.getName();

        if (authUserEmail.equals(email)) {
            return ResponseEntity.badRequest().body("User cannot connect to themselves");
        }

        var authUserOpt = userService.findByEmail(authUserEmail);
        var targetUserOpt = userService.findByEmail(email);

        if (authUserOpt.isEmpty() || targetUserOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        var authUser = authUserOpt.get();
        var targetUser = targetUserOpt.get();

        if (connectionService.checkConnection(targetUser, authUser)) {
            return ResponseEntity.ok().body("Warn: Connection already sent to you");
        }
        if (connectionService.checkConnection(authUser, targetUser)) {
            return ResponseEntity.ok().body("Warn: Connection already exists");
        }

        connectionService.createConnection(authUser, targetUser);
        return ResponseEntity.ok().body("Connection request sent");
    }
}
