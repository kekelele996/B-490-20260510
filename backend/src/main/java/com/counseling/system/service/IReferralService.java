package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.counseling.system.entity.ReferralRecord;
import java.util.List;

public interface IReferralService extends IService<ReferralRecord> {
    String generateInviteCode(Long userId);
    Long resolveInviterByCode(String inviteCode);
    ReferralRecord createReferralRecord(Long inviterId, Long inviteeId);
    void processReferralReward(Long appointmentId, Long inviteeId);
    List<ReferralRecord> getMyReferrals(Long inviterId);
    Double getReferralRewardAmount();
}
