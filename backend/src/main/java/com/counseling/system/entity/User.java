package com.counseling.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    @JsonIgnore
    private String password;
    private String phone;
    private String email;
    private String nickname;
    private String gender;
    private LocalDate birthday;
    private String avatar;
    private String role; // USER, COUNSELOR, ADMIN
    private Double balance = 0.0;
    private String inviteCode;
    private Long inviterId;
}
