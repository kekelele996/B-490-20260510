package com.counseling.system.security;

import com.counseling.system.entity.User;
import com.counseling.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            authenticate(accessor);
        } else if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
            Authentication authentication = toAuthentication(accessor);
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new AccessDeniedException("WebSocket未认证");
            }
            if (StompCommand.SUBSCRIBE.equals(command)) {
                validateSubscribeDestination(accessor, authentication);
            }
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AccessDeniedException("缺少有效Token");
        }
        String token = authHeader.substring(7);
        String username;
        try {
            username = jwtUtils.extractUsername(token);
        } catch (Exception e) {
            throw new AccessDeniedException("Token无效");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtUtils.validateToken(token, userDetails.getUsername())) {
            throw new AccessDeniedException("Token无效或已过期");
        }
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(),
                null,
                userDetails.getAuthorities()
        ));
    }

    private void validateSubscribeDestination(StompHeaderAccessor accessor, Authentication authentication) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }
        if (!destination.startsWith("/queue/messages/") && !destination.startsWith("/queue/notifications/")) {
            return;
        }

        if (isAdmin(authentication)) {
            return;
        }

        Long targetUserId = parseTrailingId(destination);
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("无效用户"));
        if (!currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("无权订阅他人队列");
        }
    }

    private Authentication toAuthentication(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof Authentication) {
            return (Authentication) accessor.getUser();
        }
        return null;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private Long parseTrailingId(String destination) {
        int idx = destination.lastIndexOf('/');
        if (idx < 0 || idx == destination.length() - 1) {
            throw new AccessDeniedException("非法订阅目标");
        }
        try {
            return Long.parseLong(destination.substring(idx + 1));
        } catch (NumberFormatException e) {
            throw new AccessDeniedException("非法订阅目标");
        }
    }
}
