package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.dto.AppointmentRequest;
import com.counseling.system.entity.Appointment;
import com.counseling.system.entity.Counselor;
import com.counseling.system.entity.User;
import com.counseling.system.repository.CounselorRepository;
import com.counseling.system.security.SecurityHelper;
import com.counseling.system.service.IAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private IAppointmentService appointmentService;

    @Autowired
    private CounselorRepository counselorRepository;

    @Autowired
    private SecurityHelper securityHelper;

    private static final Set<String> ALLOWED_STATUSES = Set.of("PENDING", "CONFIRMED", "COMPLETED", "REJECTED", "CANCELLED");

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Appointment>> getMyAppointments(@RequestParam(required = false) Long userId, Authentication authentication) {
        Long targetUserId = resolveTargetUserId(authentication, userId);
        return ApiResponse.success(appointmentService.getMyAppointments(targetUserId));
    }
    
    @GetMapping("/counselor")
    @PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
    public ApiResponse<List<Appointment>> getCounselorAppointments(@RequestParam Long counselorId, Authentication authentication) {
        if (!securityHelper.isAdmin(authentication)) {
            User currentUser = securityHelper.currentUser(authentication);
            Counselor counselor = counselorRepository.selectById(counselorId);
            if (counselor == null || counselor.getUserId() == null || !counselor.getUserId().equals(currentUser.getId())) {
                throw new AccessDeniedException("无权查看其他咨询师预约");
            }
        }
        return ApiResponse.success(appointmentService.getCounselorAppointments(counselorId));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Appointment> createAppointment(@Valid @RequestBody AppointmentRequest request, Authentication authentication) {
        User currentUser = securityHelper.currentUser(authentication);
        if (request.getUserId() != null && !request.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("不能为其他用户创建预约");
        }
        return ApiResponse.success(appointmentService.createAppointment(currentUser.getId(), request.getCounselorId(), request.getAppointmentTime()));
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Appointment> updateStatus(@PathVariable Long id, @RequestParam String status, Authentication authentication) {
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new RuntimeException("非法状态值");
        }
        Appointment appointment = getAppointmentOrThrow(id);
        if (securityHelper.isAdmin(authentication)) {
            return ApiResponse.success(appointmentService.updateStatus(id, status));
        }

        User currentUser = securityHelper.currentUser(authentication);
        boolean isAppointmentUser = appointment.getUserId() != null && appointment.getUserId().equals(currentUser.getId());
        Counselor counselor = appointment.getCounselorId() == null ? null : counselorRepository.selectById(appointment.getCounselorId());
        boolean isAppointmentCounselor = counselor != null
                && counselor.getUserId() != null
                && counselor.getUserId().equals(currentUser.getId());

        if (isAppointmentUser) {
            if (!"CANCELLED".equals(status)) {
                throw new AccessDeniedException("用户仅可取消自己的预约");
            }
            return ApiResponse.success(appointmentService.updateStatus(id, status));
        }

        if (isAppointmentCounselor) {
            if (!Set.of("CONFIRMED", "REJECTED", "COMPLETED").contains(status)) {
                throw new AccessDeniedException("咨询师仅可确认/拒绝/完成自己的预约");
            }
            return ApiResponse.success(appointmentService.updateStatus(id, status));
        }

        throw new AccessDeniedException("无权操作该预约");
    }

    private Appointment getAppointmentOrThrow(Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            throw new RuntimeException("未找到该预约记录");
        }
        return appointment;
    }

    private Long resolveTargetUserId(Authentication authentication, Long requestedUserId) {
        User currentUser = securityHelper.currentUser(authentication);
        if (securityHelper.isAdmin(authentication)) {
            return requestedUserId != null ? requestedUserId : currentUser.getId();
        }
        if (requestedUserId != null && !requestedUserId.equals(currentUser.getId())) {
            throw new AccessDeniedException("无权查看他人预约");
        }
        return currentUser.getId();
    }

    private void assertAppointmentUserOrAdmin(Long appointmentId, Authentication authentication) {
        if (securityHelper.isAdmin(authentication)) {
            return;
        }
        Appointment appointment = getAppointmentOrThrow(appointmentId);
        Long currentUserId = securityHelper.currentUser(authentication).getId();
        if (appointment.getUserId() == null || !appointment.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("无权操作该预约");
        }
    }

    private void assertAppointmentCounselorOrAdmin(Long appointmentId, Authentication authentication) {
        if (securityHelper.isAdmin(authentication)) {
            return;
        }
        Appointment appointment = getAppointmentOrThrow(appointmentId);
        Counselor counselor = appointment.getCounselorId() == null ? null : counselorRepository.selectById(appointment.getCounselorId());
        Long currentUserId = securityHelper.currentUser(authentication).getId();
        if (counselor == null || counselor.getUserId() == null || !counselor.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("无权操作该预约");
        }
    }

    @PutMapping("/{id}/rate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<Appointment> rateAppointment(@PathVariable Long id,
                                                    @Valid @RequestBody com.counseling.system.dto.RatingRequest request,
                                                    Authentication authentication) {
        assertAppointmentUserOrAdmin(id, authentication);
        return ApiResponse.success(appointmentService.rateAppointment(id, request.getRating(), request.getFeedback()));
    }

    @PutMapping("/{id}/reply")
    @PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
    public ApiResponse<Appointment> replyToAppointment(@PathVariable Long id,
                                                       @RequestBody java.util.Map<String, String> body,
                                                       Authentication authentication) {
        assertAppointmentCounselorOrAdmin(id, authentication);
        return ApiResponse.success(appointmentService.replyToFeedback(id, body.get("reply")));
    }
}
