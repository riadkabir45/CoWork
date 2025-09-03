package com.aritra.d.riad.CoWork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aritra.d.riad.CoWork.model.Notification;
import com.aritra.d.riad.CoWork.model.Users;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByRecipient(Users user);
    List<Notification> findByRecipientAndRead(Users user, boolean read);
    long countByRecipient(Users user);
    long countByRecipientAndRead(Users user, boolean read);
    List<Notification> findByRead(boolean read);
}
