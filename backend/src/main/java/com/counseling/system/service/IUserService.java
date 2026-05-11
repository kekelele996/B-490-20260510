package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.counseling.system.entity.User;
import com.counseling.system.dto.LoginRequest;
import com.counseling.system.dto.RegisterRequest;

public interface IUserService extends IService<User> {
    User login(LoginRequest request);
    User register(RegisterRequest request);
    User getUserById(Long id);
    User updateUser(Long id, User userDetails);
    String getOrCreateInviteCode(Long userId);
}
