package com.counseling.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.counseling.system.entity.InviteRelation;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InviteRelationRepository extends BaseMapper<InviteRelation> {
    default List<InviteRelation> findByInviterId(Long inviterId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InviteRelation>()
                .eq(InviteRelation::getInviterId, inviterId)
                .orderByDesc(InviteRelation::getCreateTime));
    }

    default Optional<InviteRelation> findByInviteeId(Long inviteeId) {
        return Optional.ofNullable(selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InviteRelation>()
                .eq(InviteRelation::getInviteeId, inviteeId)));
    }
}
