package com.aritra.d.riad.CoWork.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aritra.d.riad.CoWork.dto.ConnectionDTO;
import com.aritra.d.riad.CoWork.model.Connections;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.ConnectionRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ConnectionService {
    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private UserService userService;

    public void createConnection(Users sender, Users receiver) {
        Connections connection = new Connections();
        connection.setSender(sender);
        connection.setReceiver(receiver);
        connectionRepository.save(connection);
    }

    public void acceptConnection(Connections connection) {
        connection.setAccepted(true);
        connectionRepository.save(connection);
    }

    public boolean checkConnection(Users user1, Users user2) {
        return connectionRepository.findBySenderAndReceiver(user1, user2) != null;
    }

    public List<Connections> getUserConnections(Users recvUsers) {
        return connectionRepository.findByReceiver(recvUsers);
    }

    public List<ConnectionDTO> getUserConnectionsDTO(Users recvUsers) {
        return connectionRepository.findByReceiver(recvUsers).stream()
                .map(this::generateConnectionsDTO)
                .toList();
    }

    private ConnectionDTO generateConnectionsDTO(Connections connection) {
        ConnectionDTO dto = new ConnectionDTO();
        dto.setId(connection.getId());
        dto.setSender(userService.generateSimpleUserDTO(connection.getSender()));
        dto.setReceiver(userService.generateSimpleUserDTO(connection.getReceiver()));
        dto.setAccepted(connection.isAccepted());
        dto.setUpDateTime(connection.getUpdatedAt());
        return dto; 
    }
}
