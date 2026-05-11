package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.entity.Transaction;
import com.counseling.system.entity.User;
import com.counseling.system.security.SecurityHelper;
import com.counseling.system.service.IUserService;
import com.counseling.system.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private SecurityHelper securityHelper;

    @PostMapping("/topup")
    @PreAuthorize("hasAnyRole('USER', 'COUNSELOR', 'ADMIN')")
    @Transactional
    public ApiResponse<User> topUp(@RequestParam Double amount,
                                   @RequestParam(required = false) Long userId,
                                   Authentication authentication) {
        Long targetUserId = resolveTargetUserId(authentication, userId);
        User user = userService.getById(targetUserId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (amount <= 0) {
            throw new RuntimeException("金额必须大于0");
        }
        
        user.setBalance((user.getBalance() == null ? 0.0 : user.getBalance()) + amount);
        userService.updateById(user);

        transactionService.recordTransaction(targetUserId, amount, "TOPUP", "SUCCESS", "余额充值");

        return ApiResponse.success(user);
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('USER', 'COUNSELOR', 'ADMIN')")
    public ApiResponse<Double> getBalance(@RequestParam(required = false) Long userId, Authentication authentication) {
        Long targetUserId = resolveTargetUserId(authentication, userId);
        User user = userService.getById(targetUserId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return ApiResponse.success(user.getBalance() == null ? 0.0 : user.getBalance());
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('USER', 'COUNSELOR', 'ADMIN')")
    public ApiResponse<List<Transaction>> getTransactions(@RequestParam(required = false) Long userId, Authentication authentication) {
        Long targetUserId = resolveTargetUserId(authentication, userId);
        return ApiResponse.success(transactionService.getTransactionsByUserId(targetUserId));
    }

    private Long resolveTargetUserId(Authentication authentication, Long requestedUserId) {
        User currentUser = securityHelper.currentUser(authentication);
        if (securityHelper.isAdmin(authentication)) {
            return requestedUserId != null ? requestedUserId : currentUser.getId();
        }
        if (requestedUserId != null && !requestedUserId.equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("无权操作他人钱包");
        }
        return currentUser.getId();
    }
}


