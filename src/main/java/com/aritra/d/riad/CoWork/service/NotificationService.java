package com.aritra.d.riad.CoWork.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.aritra.d.riad.CoWork.model.Notification;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.NotificationRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserService userService;


    public Notification sendNotification(String email, String title, String message) {
        Users user = userService.findByEmail(email).orElseThrow();
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setMessage(message);
        notification.setTitle(title);
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String email) {
        Users user = userService.findByEmail(email).orElseThrow();
        return notificationRepository.findByRecipient(user);
    }

    public long getUserUnreadNotificationCount(String email) {
        Users user = userService.findByEmail(email).orElseThrow();
        return notificationRepository.countByRecipientAndRead(user, false);
    }
    public Notification sendNotification(Users user, String title, String message) {
        try{
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setMessage(message);
            notification.setTitle(title);
            return notificationRepository.save(notification);
        } catch (DataIntegrityViolationException e) {
            log.error("Duplicate notification for user: {}", user.getEmail());
        }
        return null;
    }

    public List<Notification> getUserNotifications(Users user) {
        return notificationRepository.findByRecipient(user);
    }

    public List<Notification> getUserUnreadNotifications(Users user) {
        return notificationRepository.findByRecipientAndRead(user, false);
    }

    public long getUserUnreadNotificationCount(Users user) {
        return notificationRepository.countByRecipientAndRead(user, false);
    }

    public Notification markNotificationRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void deleteSeenNotifications() {
        List<Notification> seenNotifications = notificationRepository.findByRead(true);
        notificationRepository.deleteAll(seenNotifications);
    }

}
