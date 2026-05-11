package com.counseling.system.security;

import com.counseling.system.entity.User;
import com.counseling.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SecurityHelper {

    @Autowired
    private UserRepository userRepository;

    public User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("未登录");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("无效用户"));
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public void assertSelfOrAdmin(Long userId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return;
        }
        User current = currentUser(authentication);
        if (!current.getId().equals(userId)) {
            throw new AccessDeniedException("无权访问他人数据");
        }
    }
}
