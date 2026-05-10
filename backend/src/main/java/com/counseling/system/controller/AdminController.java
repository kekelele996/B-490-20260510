package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.entity.Counselor;
import com.counseling.system.entity.User;
import com.counseling.system.entity.SystemSetting;
import com.counseling.system.entity.Appointment;
import com.counseling.system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ICounselorService counselorService;
    
    @Autowired
    private ISystemSettingService systemSettingService;

    @Autowired
    private IAppointmentService appointmentService;

    @GetMapping("/users")
    public ApiResponse<List<User>> getAllUsers() {
        return ApiResponse.success(userService.list());
    }

    @PostMapping("/users")
    public ApiResponse<User> createUser(@RequestBody User user) {
        user.setPassword("123456"); // Default password
        userService.save(user);
        return ApiResponse.success(user);
    }

    @PutMapping("/users/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User user = userService.getById(id);
        if (user == null) throw new RuntimeException("未找到该用户");
        user.setNickname(userDetails.getNickname());
        user.setRole(userDetails.getRole());
        user.setPhone(userDetails.getPhone());
        user.setEmail(userDetails.getEmail());
        user.setGender(userDetails.getGender());
        user.setAvatar(userDetails.getAvatar());
        if(userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
             user.setPassword(userDetails.getPassword());
        }
        userService.updateById(user);
        return ApiResponse.success(user);
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/counselors")
    public ApiResponse<List<Counselor>> getAllCounselors() {
        return ApiResponse.success(counselorService.getAllCounselors());
    }
    
    @DeleteMapping("/counselors/{id}")
    public ApiResponse<Void> deleteCounselor(@PathVariable Long id) {
        counselorService.deleteCounselor(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/counselors/{id}")
    public ApiResponse<Counselor> updateCounselor(@PathVariable Long id, @RequestBody Counselor counselorDetails) {
        return ApiResponse.success(counselorService.updateCounselor(id, counselorDetails));
    }

    @PutMapping("/counselors/{id}/approve")
    public ApiResponse<Counselor> approveCounselor(@PathVariable Long id) {
        return ApiResponse.success(counselorService.approveCounselor(id));
    }
    
    @PutMapping("/counselors/{id}/reject")
    public ApiResponse<Counselor> rejectCounselor(@PathVariable Long id) {
        return ApiResponse.success(counselorService.rejectCounselor(id));
    }

    @GetMapping("/appointments")
    public ApiResponse<List<Appointment>> getAllAppointments() {
        return ApiResponse.success(appointmentService.getAllAppointments());
    }

    @PutMapping("/appointments/{id}")
    public ApiResponse<Appointment> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointmentDetails) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) throw new RuntimeException("未找到该预约记录");
        appointment.setAppointmentTime(appointmentDetails.getAppointmentTime());
        appointment.setStatus(appointmentDetails.getStatus());
        appointmentService.updateById(appointment);
        return ApiResponse.success(appointment);
    }
    
    @DeleteMapping("/appointments/{id}")
    public ApiResponse<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.removeById(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/settings")
    public ApiResponse<List<SystemSetting>> getSettings() {
        return ApiResponse.success(systemSettingService.list());
    }

    @PostMapping("/settings")
    public ApiResponse<SystemSetting> updateSetting(@RequestBody SystemSetting setting) {
        SystemSetting existing = systemSettingService.getByKey(setting.getSettingKey());
        if (existing != null) {
            existing.setSettingValue(setting.getSettingValue());
            existing.setDescription(setting.getDescription());
            systemSettingService.updateById(existing);
            return ApiResponse.success(existing);
        } else {
            systemSettingService.save(setting);
            return ApiResponse.success(setting);
        }
    }
    
    @DeleteMapping("/settings/{id}")
    public ApiResponse<Void> deleteSetting(@PathVariable Long id) {
        systemSettingService.removeById(id);
        return ApiResponse.success(null);
    }
}
