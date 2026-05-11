package com.counseling.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("transactions")
public class Transaction {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Double amount;
    private String type; // TOPUP, PAYMENT, REFUND
    private String status; // SUCCESS, FAILED
    private LocalDateTime createTime;
    private String description;
}
