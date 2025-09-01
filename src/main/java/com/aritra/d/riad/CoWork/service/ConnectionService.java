package com.aritra.d.riad.CoWork.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aritra.d.riad.CoWork.model.Connections;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.ConnectionRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ConnectionService {
    @Autowired
    private ConnectionRepository connectionRepository;

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
}
