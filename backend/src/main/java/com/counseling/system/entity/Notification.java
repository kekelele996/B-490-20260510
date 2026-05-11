package com.counseling.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notifications")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String content;
    private Boolean isRead = false;
    private LocalDateTime createTime = LocalDateTime.now();
    private String type; 
    private Long relatedId;
}
