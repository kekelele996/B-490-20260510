package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.counseling.system.entity.Appointment;
import java.time.LocalDateTime;
import java.util.List;

public interface IAppointmentService extends IService<Appointment> {
    List<Appointment> getAllAppointments();
    List<Appointment> getMyAppointments(Long userId);
    List<Appointment> getCounselorAppointments(Long counselorId);
    Appointment createAppointment(Long userId, Long counselorId, LocalDateTime time);
    Appointment updateStatus(Long id, String status);
    Appointment rateAppointment(Long id, Integer rating, String feedback);
    Appointment replyToFeedback(Long id, String reply);
}
