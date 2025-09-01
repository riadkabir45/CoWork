package com.aritra.d.riad.CoWork.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.Connections;
import com.aritra.d.riad.CoWork.model.Users;

@Repository
public interface ConnectionRepository extends JpaRepository<Connections, String> {

    Connections findBySenderAndReceiver(Users user1, Users user2);

}