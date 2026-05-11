package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.counseling.system.entity.Notification;
import java.util.List;

public interface INotificationService extends IService<Notification> {
    void createNotification(Long userId, String content, String type, Long relatedId);
    List<Notification> getMyNotifications(Long userId);
    void markAsRead(Long id);
    long getUnreadCount(Long userId);
}
