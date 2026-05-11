package com.counseling.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("invite_relations")
public class InviteRelation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inviterId;

    @TableField(exist = false)
    private User inviter;

    private Long inviteeId;

    @TableField(exist = false)
    private User invitee;

    private LocalDateTime createTime;
    private Boolean rewardGiven = false;
    private Double rewardAmount;
    private LocalDateTime rewardTime;
}
