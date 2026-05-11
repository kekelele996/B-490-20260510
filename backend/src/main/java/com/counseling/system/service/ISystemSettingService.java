package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.counseling.system.entity.SystemSetting;

public interface ISystemSettingService extends IService<SystemSetting> {
    SystemSetting getByKey(String key);
}
