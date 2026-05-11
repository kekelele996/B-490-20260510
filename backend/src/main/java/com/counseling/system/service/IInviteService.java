package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.counseling.system.entity.InviteRelation;

import java.util.List;

public interface IInviteService extends IService<InviteRelation> {
    String generateInviteCode(Long userId);
    void bindInviteRelation(Long inviteeId, String inviteCode);
    void processFirstOrderReward(Long inviteeId);
    List<InviteRelation> getMyInviteList(Long inviterId);
    InviteRelation getInviteRelationByInviteeId(Long inviteeId);
}
