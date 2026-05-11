package com.counseling.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.counseling.system.entity.SystemSetting;
import com.counseling.system.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingService extends ServiceImpl<SystemSettingRepository, SystemSetting> implements ISystemSettingService {
    @Override
    public SystemSetting getByKey(String key) {
        return getOne(new LambdaQueryWrapper<SystemSetting>().eq(SystemSetting::getSettingKey, key));
    }
}
