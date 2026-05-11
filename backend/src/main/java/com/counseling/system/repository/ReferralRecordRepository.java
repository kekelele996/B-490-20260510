package com.counseling.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.counseling.system.entity.ReferralRecord;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralRecordRepository extends BaseMapper<ReferralRecord> {

    default List<ReferralRecord> findByInviterId(Long inviterId) {
        return selectList(new LambdaQueryWrapper<ReferralRecord>()
                .eq(ReferralRecord::getInviterId, inviterId)
                .orderByDesc(ReferralRecord::getCreateTime));
    }

    default List<ReferralRecord> findByInviteeId(Long inviteeId) {
        return selectList(new LambdaQueryWrapper<ReferralRecord>()
                .eq(ReferralRecord::getInviteeId, inviteeId));
    }

    default List<ReferralRecord> findByInviteeIdAndStatus(Long inviteeId, String status) {
        return selectList(new LambdaQueryWrapper<ReferralRecord>()
                .eq(ReferralRecord::getInviteeId, inviteeId)
                .eq(ReferralRecord::getStatus, status));
    }
}
