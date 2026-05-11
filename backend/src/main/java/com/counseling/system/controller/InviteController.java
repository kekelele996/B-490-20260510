package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.entity.InviteRelation;
import com.counseling.system.entity.User;
import com.counseling.system.security.SecurityHelper;
import com.counseling.system.service.IInviteService;
import com.counseling.system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, String>> getMyInviteCode(Authentication authentication) {
        try {
            User user = securityHelper.currentUser(authentication);
            String code = userService.getOrCreateInviteCode(user.getId());
            Map<String, String> result = new HashMap<>();
            result.put("inviteCode", code);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/my-invites")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<InviteRelation>> getMyInviteList(Authentication authentication) {
        try {
            User user = securityHelper.currentUser(authentication);
            List<InviteRelation> list = inviteService.getMyInviteList(user.getId());
            return ApiResponse.success(list);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/bind")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> bindInviteCode(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            User user = securityHelper.currentUser(authentication);
            String inviteCode = request.get("inviteCode");
            inviteService.bindInviteRelation(user.getId(), inviteCode);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
