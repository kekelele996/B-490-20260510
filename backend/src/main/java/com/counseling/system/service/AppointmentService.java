package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.counseling.system.entity.Appointment;
import com.counseling.system.entity.Counselor;
import com.counseling.system.entity.TimeSlot;
import com.counseling.system.entity.User;
import com.counseling.system.repository.AppointmentRepository;
import com.counseling.system.repository.CounselorRepository;
import com.counseling.system.repository.TimeSlotRepository;
import com.counseling.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AppointmentService extends ServiceImpl<AppointmentRepository, Appointment> implements IAppointmentService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CounselorRepository counselorRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = list();
        for (Appointment app : appointments) {
            if (app.getUser() == null && app.getUserId() != null) {
                app.setUser(userRepository.selectById(app.getUserId()));
            }
            if (app.getCounselor() == null && app.getCounselorId() != null) {
                Counselor c = counselorRepository.selectById(app.getCounselorId());
                if (c != null && c.getUserId() != null) {
                    c.setUser(userRepository.selectById(c.getUserId()));
                }
                app.setCounselor(c);
            }
        }
        return appointments;
    }

    @Override
    public List<Appointment> getMyAppointments(Long userId) {
        List<Appointment> list = baseMapper.findByUserId(userId);
        for (Appointment app : list) {
            populateAssociations(app);
        }
        return list;
    }

    @Override
    public List<Appointment> getCounselorAppointments(Long counselorId) {
        List<Appointment> list = baseMapper.findByCounselorId(counselorId);
        for (Appointment app : list) {
            populateAssociations(app);
        }
        return list;
    }

    private void populateAssociations(Appointment app) {
        if (app.getUserId() != null) {
            app.setUser(userRepository.selectById(app.getUserId()));
        }
        if (app.getCounselorId() != null) {
            Counselor c = counselorRepository.selectById(app.getCounselorId());
            if (c != null && c.getUserId() != null) {
                c.setUser(userRepository.selectById(c.getUserId()));
            }
            app.setCounselor(c);
        }
    }

    @Override
    public Appointment createAppointment(Long userId, Long counselorId, LocalDateTime time) {
        User user = userRepository.selectById(userId);
        if (user == null) throw new RuntimeException("未找到该用户");
        if (time == null) throw new RuntimeException("预约时间不能为空");
        
        Counselor counselor = counselorRepository.selectById(counselorId);
        if (counselor == null) throw new RuntimeException("未找到该咨询师");
        
        synchronized (this) {
            List<TimeSlot> slots = timeSlotRepository.findByCounselorIdAndStatusOrderByStartTime(counselorId, "AVAILABLE");
            TimeSlot matchedSlot = null;
            for (TimeSlot slot : slots) {
                if (slot.getStartTime() != null && slot.getStartTime().isEqual(time)) {
                    matchedSlot = slot;
                    break;
                }
            }

            if (matchedSlot == null) {
                throw new RuntimeException("所选时间段不可用，请刷新后重试");
            }
            if (matchedSlot.getStartTime() == null || !matchedSlot.getStartTime().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("预约时间必须是将来时间");
            }

            matchedSlot.setStatus("BOOKED");
            timeSlotRepository.updateById(matchedSlot);
        }

        Double fee = counselor.getFee();
        if (fee == null || fee < 0) fee = 0.0;
        
        if (user.getBalance() == null || user.getBalance() < fee) {
            throw new RuntimeException("余额不足，请先充值");
        }
        
        user.setBalance(user.getBalance() - fee);
        userRepository.updateById(user);

        Appointment appointment = new Appointment();
        appointment.setUserId(userId);
        appointment.setCounselorId(counselorId);
        appointment.setUser(user);
        appointment.setCounselor(counselor);
        appointment.setAppointmentTime(time);
        appointment.setStatus("PENDING");
        
        save(appointment);
        
        notificationService.createNotification(
            counselor.getUserId(),
            "您有一个新的预约申请: " + user.getNickname(),
            "NEW_APPOINTMENT",
            appointment.getId()
        );
        
        return appointment;
    }
    
    @Override
    public Appointment updateStatus(Long id, String status) {
        Appointment appointment = getById(id);
        if (appointment == null) throw new RuntimeException("未找到该预约记录");
        populateAssociations(appointment);
        
        if ("COMPLETED".equals(status) && "COMPLETED".equals(appointment.getStatus())) {
            return appointment;
        }

        if ("CANCELLED".equals(status) || "REJECTED".equals(status)) {
            User user = appointment.getUser();
            Counselor counselor = appointment.getCounselor();
            if (user != null && counselor != null) {
                Double fee = counselor.getFee() != null ? counselor.getFee() : 0.0;
                
                if (!"CANCELLED".equals(appointment.getStatus()) && !"REJECTED".equals(appointment.getStatus())) {
                     user.setBalance((user.getBalance() == null ? 0.0 : user.getBalance()) + fee);
                     userRepository.updateById(user);
                     
                     notificationService.createNotification(
                        user.getId(),
                        "您的预约已被取消/拒绝，费用已退回账户",
                        "WALLET",
                        appointment.getId()
                     );
                }
            }

            List<TimeSlot> slots = timeSlotRepository.findByCounselorIdOrderByStartTime(appointment.getCounselor().getId());
            for (TimeSlot slot : slots) {
                if (slot.getStartTime().isEqual(appointment.getAppointmentTime()) && "BOOKED".equals(slot.getStatus())) {
                    slot.setStatus("AVAILABLE");
                    timeSlotRepository.updateById(slot);
                    break;
                }
            }
        }

        appointment.setStatus(status);
        if ("CONFIRMED".equals(status) && appointment.getUserId() != null) {
            appointment.setMeetingLink("https://meeting.example.com/" + java.util.UUID.randomUUID().toString());
             notificationService.createNotification(
                appointment.getUserId(),
                "您的预约已确认，请准时参加",
                "APPOINTMENT_STATUS",
                appointment.getId()
             );
        } else if ("COMPLETED".equals(status)) {
            Counselor counselor = appointment.getCounselor();
            if (counselor != null) {
                Double fee = counselor.getFee();
                if (fee != null) {
                    Double currentRevenue = counselor.getTotalRevenue() != null ? counselor.getTotalRevenue() : 0.0;
                    counselor.setTotalRevenue(currentRevenue + fee);
                    counselorRepository.updateById(counselor);
                }
            }
            if (appointment.getUserId() != null) {
                notificationService.createNotification(
                    appointment.getUserId(),
                    "咨询已完成，请对本次服务进行评价",
                    "APPOINTMENT_STATUS",
                    appointment.getId()
                );
            }
        } else if ("REJECTED".equals(status) && appointment.getUserId() != null) {
             notificationService.createNotification(
                appointment.getUserId(),
                "您的预约申请被拒绝",
                "APPOINTMENT_STATUS",
                appointment.getId()
             );
        }
        updateById(appointment);
        return appointment;
    }

    @Override
    public Appointment rateAppointment(Long id, Integer rating, String feedback) {
        Appointment appointment = getById(id);
        if (appointment == null) throw new RuntimeException("Appointment not found");
        if (!"COMPLETED".equals(appointment.getStatus())) {
             throw new RuntimeException("Can only rate completed appointments");
        }
        appointment.setRating(rating);
        appointment.setFeedback(feedback);
        updateById(appointment);
        return appointment;
    }

    @Override
    public Appointment replyToFeedback(Long id, String reply) {
        Appointment appointment = getById(id);
        if (appointment == null) throw new RuntimeException("Appointment not found");
        if (appointment.getRating() == null) {
             throw new RuntimeException("Cannot reply to an appointment without user feedback");
        }
        appointment.setCounselorReply(reply);
        appointment.setCounselorReplyTime(LocalDateTime.now());
        updateById(appointment);
        return appointment;
    }
}
