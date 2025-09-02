package com.aritra.d.riad.CoWork.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aritra.d.riad.CoWork.model.Notification;
import com.aritra.d.riad.CoWork.service.NotificationService;
import com.aritra.d.riad.CoWork.service.UserService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;


    @GetMapping
    public List<Notification> getNotifications() {
        return notificationService.getUserUnreadNotifications(userService.authUser());
    }

    @GetMapping("/count")
    public long getNotificationCount() {
        return notificationService.getUserUnreadNotificationCount(userService.authUser());
    }

    @GetMapping("/{id}")
    public Notification getNotification(@PathVariable String id) {
        return notificationService.markNotificationRead(id);
    }
}
