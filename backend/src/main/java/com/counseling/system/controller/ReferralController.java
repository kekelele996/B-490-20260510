package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.entity.ReferralRecord;
import com.counseling.system.entity.User;
import com.counseling.system.security.SecurityHelper;
import com.counseling.system.service.IReferralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/referral")
public class ReferralController {

    @Autowired
    private IReferralService referralService;

    @Autowired
    private SecurityHelper securityHelper;

    @GetMapping("/invite-code")
    @PreAuthorize("hasAnyRole('USER', 'COUNSELOR', 'ADMIN')")
    public ApiResponse<Map<String, String>> getInviteCode(Authentication authentication) {
        User user = securityHelper.currentUser(authentication);
        String code = referralService.generateInviteCode(user.getId());
        Map<String, String> result = new HashMap<>();
        result.put("inviteCode", code);
        return ApiResponse.success(result);
    }

    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('USER', 'COUNSELOR', 'ADMIN')")
    public ApiResponse<List<ReferralRecord>> getMyReferrals(Authentication authentication) {
        User user = securityHelper.currentUser(authentication);
        return ApiResponse.success(referralService.getMyReferrals(user.getId()));
    }

    @GetMapping("/reward-amount")
    @PreAuthorize("hasAnyRole('USER', 'COUNSELOR', 'ADMIN')")
    public ApiResponse<Map<String, Double>> getRewardAmount() {
        Map<String, Double> result = new HashMap<>();
        result.put("rewardAmount", referralService.getReferralRewardAmount());
        return ApiResponse.success(result);
    }
}
