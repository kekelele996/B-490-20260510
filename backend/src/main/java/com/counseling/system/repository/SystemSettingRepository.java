package com.counseling.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.counseling.system.entity.SystemSetting;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemSettingRepository extends BaseMapper<SystemSetting> {
    default SystemSetting findBySettingKey(String settingKey) {
        return selectOne(new LambdaQueryWrapper<SystemSetting>().eq(SystemSetting::getSettingKey, settingKey));
    }
}
