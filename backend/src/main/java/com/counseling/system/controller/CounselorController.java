package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.entity.Counselor;
import com.counseling.system.entity.User;
import com.counseling.system.repository.CounselorRepository;
import com.counseling.system.repository.UserRepository;
import com.counseling.system.security.SecurityHelper;
import com.counseling.system.service.ICounselorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/counselors")
public class CounselorController {

    @Autowired
    private ICounselorService counselorService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CounselorRepository counselorRepository;

    @Autowired
    private SecurityHelper securityHelper;

    @GetMapping
    public ApiResponse<List<Counselor>> getAllCounselors() {
        return ApiResponse.success(counselorService.getAllCounselors());
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Counselor> getCounselor(@PathVariable Long id) {
        return ApiResponse.success(counselorService.getById(id));
    }

    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Counselor> apply(@RequestBody Counselor counselor, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未登录");
        }
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("无效用户"));
        return ApiResponse.success(counselorService.applyForCounselor(currentUser.getId(), counselor));
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
    public ApiResponse<Double> withdraw(@PathVariable Long id, Authentication authentication) {
        assertCounselorSelfOrAdmin(id, authentication);
        return ApiResponse.success(counselorService.withdrawRevenue(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
    public ApiResponse<Counselor> update(@PathVariable Long id, @RequestBody Counselor counselor, Authentication authentication) {
        assertCounselorSelfOrAdmin(id, authentication);
        return ApiResponse.success(counselorService.updateCounselor(id, counselor));
    }

    private void assertCounselorSelfOrAdmin(Long counselorId, Authentication authentication) {
        if (securityHelper.isAdmin(authentication)) {
            return;
        }
        User currentUser = securityHelper.currentUser(authentication);
        Counselor currentCounselor = counselorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("当前账号不是咨询师"));
        if (!counselorId.equals(currentCounselor.getId())) {
            throw new AccessDeniedException("无权操作其他咨询师资料");
        }
    }
}
