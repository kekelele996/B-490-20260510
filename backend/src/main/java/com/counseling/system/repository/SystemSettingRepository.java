package com.counseling.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.counseling.system.entity.SystemSetting;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingRepository extends BaseMapper<SystemSetting> {
    default SystemSetting findBySettingKey(String settingKey) {
        return selectOne(new LambdaQueryWrapper<SystemSetting>().eq(SystemSetting::getSettingKey, settingKey));
    }

    default Optional<SystemSetting> findBySettingKeyOptional(String settingKey) {
        return Optional.ofNullable(selectOne(new LambdaQueryWrapper<SystemSetting>().eq(SystemSetting::getSettingKey, settingKey)));
    }
}
