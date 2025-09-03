package com.aritra.d.riad.CoWork.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.aritra.d.riad.CoWork.model.Connections;
import com.aritra.d.riad.CoWork.model.Message;
import com.aritra.d.riad.CoWork.service.ConnectionService;
import com.aritra.d.riad.CoWork.service.MessageService;
import com.aritra.d.riad.CoWork.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TaskSchedulerService {

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ConnectionService connectionService;

    @Scheduled(fixedRate = 300000) 
    public void runEveryFiveMinutes() {
        notifyUnseenMessages();
        notifyPendingConnections();
        notificationService.deleteSeenNotifications();
        log.info("5 minute task complete");
    }

    private void notifyUnseenMessages(){
        for(Message message : messageService.getLatestUnreadMessagePerConnection()) {
            if(message.getConnections().getSender() == message.getSender())
                notificationService.sendNotification(message.getConnections().getReceiver(), "New message from " + message.getSender().getFullName(), message.getContent());
            else
                notificationService.sendNotification(message.getConnections().getSender(), "New message from " + message.getSender().getFullName(), message.getContent());
        }
    }

    private void notifyPendingConnections(){
        for(Connections connection : connectionService.getPendingConnections()) {
            notificationService.sendNotification(connection.getReceiver(), "Pending connection request", "You have a new connection request from " + connection.getSender().getFullName());
        }
    }
}
