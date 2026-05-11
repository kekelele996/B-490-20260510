package com.counseling.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("time_slots")
public class TimeSlot {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long counselorId;

    @TableField(exist = false)
    private Counselor counselor;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status = "AVAILABLE"; 
}
