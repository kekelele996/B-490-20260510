package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.counseling.system.entity.Notification;
import com.counseling.system.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService extends ServiceImpl<NotificationRepository, Notification> implements INotificationService {

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Override
    public void createNotification(Long userId, String content, String type, Long relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setContent(content);
        n.setType(type);
        n.setRelatedId(relatedId);
        save(n);
        
        // Push to WebSocket
        messagingTemplate.convertAndSend("/queue/notifications/" + userId, n);
    }

    @Override
    public List<Notification> getMyNotifications(Long userId) {
        return baseMapper.findByUserIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public void markAsRead(Long id) {
        Notification n = getById(id);
        if (n != null) {
            n.setIsRead(true);
            updateById(n);
        }
    }
    
    @Override
    public long getUnreadCount(Long userId) {
        return baseMapper.findByUserIdAndIsReadFalse(userId).size();
    }
}

