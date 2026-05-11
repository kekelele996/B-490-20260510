package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.dto.LoginRequest;
import com.counseling.system.dto.LoginResponse;
import com.counseling.system.dto.RegisterRequest;
import com.counseling.system.entity.User;
import com.counseling.system.security.JwtUtils;
import com.counseling.system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IUserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.login(request);
            String token = jwtUtils.generateToken(user.getUsername());
            return ApiResponse.success(new LoginResponse(token, user));
        } catch (Exception e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }

    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ApiResponse.success(userService.register(request));
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
