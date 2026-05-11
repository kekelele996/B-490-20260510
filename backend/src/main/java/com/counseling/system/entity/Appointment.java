package com.counseling.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("appointments")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    
    @TableField(exist = false)
    private User user;

    private Long counselorId;

    @TableField(exist = false)
    private Counselor counselor;

    private LocalDateTime appointmentTime;
    private String status;
    private String feedback;
    private Integer rating;
    private String meetingLink;
    private String counselorReply;
    private LocalDateTime counselorReplyTime;
}
