package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.counseling.system.entity.Counselor;
import java.util.List;

public interface ICounselorService extends IService<Counselor> {
    List<Counselor> getAllCounselors();
    Counselor getByUserId(Long userId);
    Counselor applyForCounselor(Long userId, Counselor counselorDetails);
    Counselor updateCounselor(Long id, Counselor counselorDetails);
    Counselor approveCounselor(Long id);
    Counselor rejectCounselor(Long id);
    void deleteCounselor(Long id);
    Double withdrawRevenue(Long id);
}
