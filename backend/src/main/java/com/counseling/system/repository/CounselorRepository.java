package com.counseling.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.counseling.system.entity.Counselor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CounselorRepository extends BaseMapper<Counselor> {
    default Optional<Counselor> findByUserId(Long userId) {
        return Optional.ofNullable(selectOne(new LambdaQueryWrapper<Counselor>().eq(Counselor::getUserId, userId)));
    }
}
