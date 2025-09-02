package com.aritra.d.riad.CoWork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.Connections;
import com.aritra.d.riad.CoWork.model.Message;
import com.aritra.d.riad.CoWork.model.Users;

@Repository
public interface MessageRepository extends JpaRepository<Message,String> {

    List<Message> findByConnections(Connections connections);

    List<Message> findByConnectionsAndSenderNotAndSeenFalse(Connections connections, Users authUser);

}
