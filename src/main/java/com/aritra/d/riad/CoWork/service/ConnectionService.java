package com.aritra.d.riad.CoWork.service;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aritra.d.riad.CoWork.dto.ConnectionDTO;
import com.aritra.d.riad.CoWork.model.Connections;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.ConnectionRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
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

    public Connections getConnectionById(String id) {
        return connectionRepository.findById(id).orElse(null);
    }

    public boolean checkConnection(Users user1, Users user2) {
        return connectionRepository.findBySenderAndReceiver(user1, user2) != null;
    }
    
    public boolean checkConnectionRecv(Users user) {
        return connectionRepository.findByReceiver(user) != null;
    }

    public boolean checkConnectionSender(Users user) {
        return connectionRepository.findBySender(user) != null;
    }

    public List<Connections> getUserConnections(Users recvUsers) {
        return connectionRepository.findByReceiver(recvUsers);
    }

    public List<ConnectionDTO> getUserConnectionsDTO(Users recvUsers) {
        return connectionRepository.findByReceiver(recvUsers).stream()
                .map(this::generateConnectionsDTO)
                .toList();
    }

    public List<Connections> getUserAcceptedConnections(Users recvUsers) {
        return Stream.concat(
                connectionRepository.findBySenderAndAccepted(recvUsers, true).stream(),
                connectionRepository.findByReceiverAndAccepted(recvUsers, true).stream()
        ).toList();
    }

    public List<ConnectionDTO> getUserAcceptedConnectionsDTO(Users recvUsers) {
        return getUserAcceptedConnections(recvUsers).stream()
                .map(this::generateConnectionsDTO)
                .toList();
    }

    public ConnectionDTO generateConnectionsDTO(Connections connection) {
        ConnectionDTO dto = new ConnectionDTO();
        dto.setId(connection.getId());
        dto.setSender(userService.generateSimpleUserDTO(connection.getSender()));
        dto.setReceiver(userService.generateSimpleUserDTO(connection.getReceiver()));
        dto.setAccepted(connection.isAccepted());
        dto.setUpDateTime(connection.getUpdatedAt());
        dto.setCurrentRating(connection.getRateing()); // Include the current rating
        Users authUser = userService.authUser();
        if(authUser ==  connection.getReceiver()) {
            dto.setMentor(connection.getSender().hasRole("MENTOR"));
        } else {
            dto.setMentor(connection.getReceiver().hasRole("MENTOR"));
        }
        return dto;
    }

    public List<Users> getAllConnectedUsers(Users recvUsers) {
        List<Connections> connectionsRecv = connectionRepository.findByReceiver(recvUsers);
        List<Connections> connectionSend = connectionRepository.findBySender(recvUsers);

        return Stream.concat(
                connectionsRecv.stream().map(Connections::getSender),
                connectionSend.stream().map(Connections::getReceiver)
        ).distinct().toList();
    }

    public List<Connections> getPendingConnections() {
        return connectionRepository.findByAccepted(false);
    }

    public void rateConnectionUser(String connectionId, Users currentUser, Integer rating, String comment) {
        // Get the connection
        Connections connection = getConnectionById(connectionId);
        if (connection == null) {
            throw new IllegalArgumentException("Connection not found");
        }
        
        // Verify that the current user is part of this connection
        if (!connection.getSender().getId().equals(currentUser.getId()) && 
            !connection.getReceiver().getId().equals(currentUser.getId())) {
            throw new SecurityException("Access denied: User is not part of this connection");
        }
        
        // Get the other user (the one being rated)
        Users otherUser = connection.getSender().getId().equals(currentUser.getId()) 
            ? connection.getReceiver() 
            : connection.getSender();
        
        // Verify the other user is a mentor
        if (!otherUser.hasRole("MENTOR")) {
            throw new IllegalArgumentException("User is not a mentor");
        }
        
        // Validate rating (-2 to 5 range)
        if (rating == null || rating < -2 || rating > 5) {
            throw new IllegalArgumentException("Invalid rating value. Rating must be between -2 and 5");
        }
        
        // Set the rating on the connection and save
        connection.setRateing(rating);
        connectionRepository.save(connection);
        
        log.info("Rating submitted: User {} rated mentor {} with {} stars. Comment: {}", 
            currentUser.getEmail(), otherUser.getEmail(), rating, comment);
        
        // TODO: Save to mentor ratings table when implemented
    }
}
