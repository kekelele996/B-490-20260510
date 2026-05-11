package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.counseling.system.entity.InviteRecord;
import com.counseling.system.entity.User;

import java.util.List;

public interface IInviteService extends IService<InviteRecord> {
    String generateInviteCode();
    User bindInviteCode(User invitee, String inviteCode);
    void processInviteRebate(Long inviteeId);
    List<InviteRecord> getMyInvites(Long inviterId);
    InviteRecord getMyInviterInfo(Long inviteeId);
}
