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
import com.aritra.d.riad.CoWork.model.Connections;
import com.aritra.d.riad.CoWork.service.ConnectionService;
import com.aritra.d.riad.CoWork.service.UserService;

@RestController
@RequestMapping("/connections")
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

    @PostMapping
    public ResponseEntity<String> acceptConnection(@RequestBody Connections connection) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authUserEmail = authentication.getName();
        //userService.findByEmail(authUserEmail).ifPresent(connectionService::checkConnection);

        var user = userService.findByEmail(authUserEmail);

        if(user.isPresent()) {
            if(connectionService.checkConnectionRecv(user.get())) {
                connectionService.acceptConnection(connection);
                return ResponseEntity.ok().body("Connection accepted");
            }
        }
        return ResponseEntity.ok().body("Warn: Invalid connection request");
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
