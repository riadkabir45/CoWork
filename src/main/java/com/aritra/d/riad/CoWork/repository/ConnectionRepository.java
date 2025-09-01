package com.aritra.d.riad.CoWork.repository;


import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.Connections;
import com.aritra.d.riad.CoWork.model.Users;

@Repository
public interface ConnectionRepository extends JpaRepository<Connections, String> {
    Connections findBySenderAndReceiver(Users user1, Users user2);
    List<Connections> findByReceiver(Users receiver);
    List<Connections> findBySender(Users sender);
    Collection<Connections> findByReceiverAndAccepted(Users recvUsers, boolean b);
    Collection<Connections> findBySenderAndAccepted(Users recvUsers, boolean b);
}