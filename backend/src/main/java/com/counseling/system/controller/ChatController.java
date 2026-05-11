package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.entity.ChatMessage;
import com.counseling.system.entity.User;
import com.counseling.system.repository.ChatMessageRepository;
import com.counseling.system.repository.UserRepository;
import com.counseling.system.security.SecurityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityHelper securityHelper;

    @MessageMapping("/chat")
    @org.springframework.transaction.annotation.Transactional
    public void processMessage(@Payload ChatMessage chatMessage, Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("未认证的聊天请求");
        }
        User sender = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new AccessDeniedException("无效用户"));
        if (chatMessage.getReceiverId() == null) {
            throw new RuntimeException("接收方不能为空");
        }
        if (chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(sender.getId())) {
            throw new AccessDeniedException("禁止伪造发送者身份");
        }
        if (userRepository.selectById(chatMessage.getReceiverId()) == null) {
            throw new RuntimeException("接收方不存在");
        }

        chatMessage.setSenderId(sender.getId());
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessageRepository.insert(chatMessage);

        messagingTemplate.convertAndSend("/queue/messages/" + chatMessage.getReceiverId(), chatMessage);
        messagingTemplate.convertAndSend("/queue/messages/" + chatMessage.getSenderId(), chatMessage);
        log.debug("Chat message dispatched. senderId={}, receiverId={}",
                chatMessage.getSenderId(), chatMessage.getReceiverId());
    }

    @GetMapping("/api/messages/{user1Id}/{user2Id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ChatMessage>> getChatHistory(@PathVariable Long user1Id,
                                                         @PathVariable Long user2Id,
                                                         Authentication authentication) {
        if (!securityHelper.isAdmin(authentication)) {
            Long currentUserId = securityHelper.currentUser(authentication).getId();
            if (!currentUserId.equals(user1Id) && !currentUserId.equals(user2Id)) {
                throw new AccessDeniedException("无权查看他人聊天记录");
            }
        }
        return ApiResponse.success(chatMessageRepository.findChatHistory(user1Id, user2Id));
    }
}
