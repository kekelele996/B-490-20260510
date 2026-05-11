package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.entity.InviteRecord;
import com.counseling.system.entity.User;
import com.counseling.system.security.SecurityHelper;
import com.counseling.system.service.IInviteService;
import com.counseling.system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invite")
public class InviteController {

    @Autowired
    private IInviteService inviteService;

    @Autowired
    private IUserService userService;

    @Autowired
    private SecurityHelper securityHelper;

    @GetMapping("/my-code")
    @PreAuthorize("hasAnyRole('USER', 'COUNSELOR', 'ADMIN')")
    public ApiResponse<String> getMyInviteCode(Authentication authentication) {
        User user = securityHelper.currentUser(authentication);
        if (user.getInviteCode() == null || user.getInviteCode().isEmpty()) {
            String newCode = generateUniqueInviteCode(user.getId());
            user.setInviteCode(newCode);
            userService.updateById(user);
        }
        return ApiResponse.success(user.getInviteCode());
    }

    private String generateUniqueInviteCode(Long excludeUserId) {
        int maxAttempts = 20;
        for (int i = 0; i < maxAttempts; i++) {
            String code = inviteService.generateInviteCode();
            User existing = userService.lambdaQuery()
                    .eq(User::getInviteCode, code)
                    .ne(User::getId, excludeUserId)
                    .one();
            if (existing == null) {
                return code;
            }
        }
        throw new RuntimeException("生成邀请码失败,请稍后重试");
    }

    @GetMapping("/my-invites")
    @PreAuthorize("hasAnyRole('USER', 'COUNSELOR', 'ADMIN')")
    public ApiResponse<List<InviteRecord>> getMyInvites(Authentication authentication) {
        User user = securityHelper.currentUser(authentication);
        return ApiResponse.success(inviteService.getMyInvites(user.getId()));
    }

    @GetMapping("/my-inviter")
    @PreAuthorize("hasAnyRole('USER', 'COUNSELOR', 'ADMIN')")
    public ApiResponse<InviteRecord> getMyInviter(Authentication authentication) {
        User user = securityHelper.currentUser(authentication);
        return ApiResponse.success(inviteService.getMyInviterInfo(user.getId()));
    }
}
