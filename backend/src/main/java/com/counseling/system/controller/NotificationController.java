package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.entity.Notification;
import com.counseling.system.security.SecurityHelper;
import com.counseling.system.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Notification>> getMy(@RequestParam(required = false) Long userId, Authentication authentication) {
        Long targetUserId = resolveTargetUserId(authentication, userId);
        return ApiResponse.success(notificationService.getMyNotifications(targetUserId));
    }
    
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> getUnreadCount(@RequestParam(required = false) Long userId, Authentication authentication) {
        Long targetUserId = resolveTargetUserId(authentication, userId);
        return ApiResponse.success(notificationService.getUnreadCount(targetUserId));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markRead(@PathVariable Long id, Authentication authentication) {
        Notification notification = notificationService.getById(id);
        if (notification == null) {
            throw new RuntimeException("通知不存在");
        }
        if (!securityHelper.isAdmin(authentication)) {
            Long currentUserId = securityHelper.currentUser(authentication).getId();
            if (!currentUserId.equals(notification.getUserId())) {
                throw new AccessDeniedException("无权操作他人通知");
            }
        }
        notificationService.markAsRead(id);
        return ApiResponse.success(null);
    }

    @Autowired
    private SecurityHelper securityHelper;

    private Long resolveTargetUserId(Authentication authentication, Long requestedUserId) {
        Long currentUserId = securityHelper.currentUser(authentication).getId();
        if (securityHelper.isAdmin(authentication)) {
            return requestedUserId != null ? requestedUserId : currentUserId;
        }
        if (requestedUserId != null && !requestedUserId.equals(currentUserId)) {
            throw new AccessDeniedException("无权查看他人通知");
        }
        return currentUserId;
    }
}
