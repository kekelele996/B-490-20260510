package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.entity.Counselor;
import com.counseling.system.entity.User;
import com.counseling.system.entity.WithdrawalRecord;
import com.counseling.system.repository.CounselorRepository;
import com.counseling.system.security.SecurityHelper;
import com.counseling.system.service.IWithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {

    @Autowired
    private IWithdrawalService withdrawalService;

    @Autowired
    private CounselorRepository counselorRepository;

    @Autowired
    private SecurityHelper securityHelper;

    @GetMapping("/counselor/{counselorId}")
    @PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
    public ApiResponse<List<WithdrawalRecord>> getMyRecords(@PathVariable Long counselorId, Authentication authentication) {
        validateCounselorAccess(counselorId, authentication);
        return ApiResponse.success(withdrawalService.getRecordsByCounselor(counselorId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
    public ApiResponse<List<WithdrawalRecord>> getCurrentCounselorRecords(@RequestParam(required = false) Long counselorId,
                                                                           Authentication authentication) {
        Long targetCounselorId = resolveTargetCounselorId(authentication, counselorId);
        return ApiResponse.success(withdrawalService.getRecordsByCounselor(targetCounselorId));
    }

    @PostMapping("/request")
    @PreAuthorize("hasRole('COUNSELOR')")
    public ApiResponse<WithdrawalRecord> requestWithdrawal(@RequestBody Map<String, Object> request,
                                                           Authentication authentication) {
        Object amountRaw = request.get("amount");
        if (amountRaw == null) {
            throw new RuntimeException("提现金额不能为空");
        }
        Double amount = Double.valueOf(amountRaw.toString());
        Long counselorId = resolveTargetCounselorId(authentication, null);
        return ApiResponse.success(withdrawalService.requestWithdrawal(counselorId, amount));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<WithdrawalRecord>> getAllRecords() {
        return ApiResponse.success(withdrawalService.getAllRecords());
    }

    @PutMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<WithdrawalRecord> approve(@PathVariable Long id) {
        return ApiResponse.success(withdrawalService.approveWithdrawal(id));
    }

    @PutMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<WithdrawalRecord> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.success(withdrawalService.rejectWithdrawal(id, body.get("reason")));
    }

    private void validateCounselorAccess(Long counselorId, Authentication authentication) {
        if (securityHelper.isAdmin(authentication)) {
            return;
        }
        Long currentCounselorId = resolveTargetCounselorId(authentication, null);
        if (!currentCounselorId.equals(counselorId)) {
            throw new AccessDeniedException("无权查看其他咨询师提现记录");
        }
    }

    private Long resolveTargetCounselorId(Authentication authentication, Long requestedCounselorId) {
        User currentUser = securityHelper.currentUser(authentication);
        if (securityHelper.isAdmin(authentication)) {
            if (requestedCounselorId != null) {
                return requestedCounselorId;
            }
            Counselor selfCounselor = counselorRepository.findByUserId(currentUser.getId()).orElse(null);
            if (selfCounselor != null) {
                return selfCounselor.getId();
            }
            throw new RuntimeException("请指定 counselorId");
        }

        Counselor counselor = counselorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("当前账号不是咨询师"));
        if (requestedCounselorId != null && !requestedCounselorId.equals(counselor.getId())) {
            throw new AccessDeniedException("无权操作其他咨询师提现");
        }
        return counselor.getId();
    }
}
