package com.counseling.system.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    // userId is resolved from authenticated principal in controller.
    private Long userId;

    @NotNull(message = "咨询师ID不能为空")
    private Long counselorId;

    @NotNull(message = "预约时间不能为空")
    private LocalDateTime appointmentTime;

    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer rating;

    private String feedback;
}
