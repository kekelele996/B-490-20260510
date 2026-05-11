package com.counseling.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.counseling.system.entity.InviteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface InviteRecordRepository extends BaseMapper<InviteRecord> {

    @Select("SELECT * FROM invite_records WHERE inviter_id = #{inviterId} ORDER BY create_time DESC")
    List<InviteRecord> findByInviterId(@Param("inviterId") Long inviterId);

    @Select("SELECT * FROM invite_records WHERE invitee_id = #{inviteeId} LIMIT 1")
    Optional<InviteRecord> findByInviteeId(@Param("inviteeId") Long inviteeId);

    @Select("SELECT * FROM invite_records WHERE inviter_id = #{inviterId} AND invitee_id = #{inviteeId} LIMIT 1")
    Optional<InviteRecord> findByInviterIdAndInviteeId(@Param("inviterId") Long inviterId, @Param("inviteeId") Long inviteeId);
}
