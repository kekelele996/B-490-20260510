package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.counseling.system.entity.Counselor;
import com.counseling.system.entity.User;
import com.counseling.system.repository.CounselorRepository;
import com.counseling.system.repository.UserRepository;
import com.counseling.system.dto.LoginRequest;
import com.counseling.system.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserRepository, User> implements IUserService {

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private CounselorRepository counselorRepository;

    @Autowired
    private IInviteService inviteService;

    private static final String INVITE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 8;

    @Override
    public User login(LoginRequest request) {
        User user = baseMapper.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // Keep role consistent with counselor audit status to avoid privilege drift.
        syncRoleWithCounselorStatus(user);
        return user;
    }

    @Override
    public User register(RegisterRequest request) {
        if (baseMapper.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setBirthday(request.getBirthday());
        user.setRole("USER");
        user.setInviteCode(generateUniqueInviteCode());
        user.setFirstOrderCompleted(false);
        save(user);

        if (request.getInviteCode() != null && !request.getInviteCode().trim().isEmpty()) {
            inviteService.bindInviteRelation(user.getId(), request.getInviteCode());
        }

        return user;
    }

    @Override
    public String getOrCreateInviteCode(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getInviteCode() == null || user.getInviteCode().isEmpty()) {
            user.setInviteCode(generateUniqueInviteCode());
            updateById(user);
        }
        return user.getInviteCode();
    }

    private String generateUniqueInviteCode() {
        Random random = new Random();
        String code;
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < INVITE_CODE_LENGTH; j++) {
                sb.append(INVITE_CODE_CHARS.charAt(random.nextInt(INVITE_CODE_CHARS.length())));
            }
            code = sb.toString();
            User existing = baseMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                    .eq(User::getInviteCode, code));
            if (existing == null) {
                return code;
            }
        }
        throw new RuntimeException("生成邀请码失败，请稍后重试");
    }

    @Override
    public User getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("未找到该用户");
        }
        return user;
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setNickname(userDetails.getNickname());
        user.setPhone(userDetails.getPhone());
        user.setEmail(userDetails.getEmail());
        user.setGender(userDetails.getGender());
        user.setBirthday(userDetails.getBirthday());
        user.setAvatar(userDetails.getAvatar());
        updateById(user);
        return user;
    }

    private void syncRoleWithCounselorStatus(User user) {
        if ("ADMIN".equals(user.getRole())) {
            return;
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
            baseMapper.updateById(user);
        }
    }
}
