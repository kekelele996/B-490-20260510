package com.counseling.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.counseling.system.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseMapper<User> {
    default Optional<User> findByUsername(String username) {
        return Optional.ofNullable(selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)));
    }

    default Optional<User> findByInviteCode(String inviteCode) {
        return Optional.ofNullable(selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getInviteCode, inviteCode)));
    }
}
