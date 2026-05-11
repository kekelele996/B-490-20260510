package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.entity.User;
import com.counseling.system.security.SecurityHelper;
import com.counseling.system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private SecurityHelper securityHelper;

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<User> getUser(@PathVariable Long id, Authentication authentication) {
        securityHelper.assertSelfOrAdmin(id, authentication);
        return ApiResponse.success(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user, Authentication authentication) {
        securityHelper.assertSelfOrAdmin(id, authentication);
        return ApiResponse.success(userService.updateUser(id, user));
    }
}
