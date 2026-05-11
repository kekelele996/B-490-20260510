package com.counseling.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("counselors")
public class Counselor {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    @TableField(exist = false)
    private User user;

    private String introduction;
    private String expertise;
    private Double fee;
    private String qualification;
    private String status;
    private String availableTime;
    private Double totalRevenue = 0.0;
}
