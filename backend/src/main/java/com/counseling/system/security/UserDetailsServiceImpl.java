package com.counseling.system.security;

import com.counseling.system.entity.Counselor;
import com.counseling.system.entity.User;
import com.counseling.system.repository.CounselorRepository;
import com.counseling.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CounselorRepository counselorRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String role = syncAndGetEffectiveRole(user);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    private String syncAndGetEffectiveRole(User user) {
        if ("ADMIN".equals(user.getRole())) {
            return "ADMIN";
        }
        Counselor counselor = counselorRepository.findByUserId(user.getId()).orElse(null);
        String expectedRole = user.getRole();
        if (counselor != null) {
            expectedRole = "APPROVED".equals(counselor.getStatus()) ? "COUNSELOR" : "USER";
        } else if ("COUNSELOR".equals(user.getRole())) {
            expectedRole = "USER";
        }
        if (!expectedRole.equals(user.getRole())) {
            user.setRole(expectedRole);
            userRepository.updateById(user);
        }
        return expectedRole;
    }
}
