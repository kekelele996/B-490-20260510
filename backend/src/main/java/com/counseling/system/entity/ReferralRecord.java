package com.counseling.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("referral_records")
public class ReferralRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inviterId;
    private Long inviteeId;
    private Long appointmentId;
    private Double rewardAmount;
    private String status; // PENDING, COMPLETED, CANCELLED
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
}
