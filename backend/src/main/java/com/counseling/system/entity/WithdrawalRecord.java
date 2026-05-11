package com.counseling.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("withdrawal_records")
public class WithdrawalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long counselorId;

    @TableField(exist = false)
    private Counselor counselor;

    private Double amount;
    private LocalDateTime requestTime;
    private String status; // PENDING, APPROVED, REJECTED
    private String note; // Admin note or rejection reason
}
