package com.counseling.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("invite_records")
public class InviteRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inviterId;
    private Long inviteeId;
    private String inviteCode;
    private String status;
    private Double rebateAmount;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;

    @TableField(exist = false)
    private User inviter;

    @TableField(exist = false)
    private User invitee;
}
