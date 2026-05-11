package com.counseling.system.controller;

import com.counseling.system.common.ApiResponse;
import com.counseling.system.entity.Counselor;
import com.counseling.system.entity.TimeSlot;
import com.counseling.system.entity.User;
import com.counseling.system.repository.CounselorRepository;
import com.counseling.system.repository.UserRepository;
import com.counseling.system.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timeslots")
public class TimeSlotController {

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CounselorRepository counselorRepository;

    @GetMapping("/counselor/{counselorId}")
    @PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
    public ApiResponse<List<TimeSlot>> getSlots(@PathVariable Long counselorId, Authentication authentication) {
        validateCounselorAccess(counselorId, authentication);
        return ApiResponse.success(timeSlotService.getSlotsByCounselor(counselorId));
    }
    
    @GetMapping("/counselor/{counselorId}/available")
    public ApiResponse<List<TimeSlot>> getAvailableSlots(@PathVariable Long counselorId) {
        return ApiResponse.success(timeSlotService.getAvailableSlotsByCounselor(counselorId));
    }

    @PostMapping("/counselor/{counselorId}")
    @PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
    public ApiResponse<TimeSlot> addSlot(@PathVariable Long counselorId, @RequestBody TimeSlot slot, Authentication authentication) {
        validateCounselorAccess(counselorId, authentication);
        return ApiResponse.success(timeSlotService.addSlot(counselorId, slot));
    }

    @PostMapping("/counselor/{counselorId}/batch")
    @PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
    public ApiResponse<List<TimeSlot>> batchAddSlots(@PathVariable Long counselorId, @RequestBody List<TimeSlot> slots, Authentication authentication) {
        validateCounselorAccess(counselorId, authentication);
        return ApiResponse.success(timeSlotService.batchAddSlots(counselorId, slots));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COUNSELOR') or hasRole('ADMIN')")
    public ApiResponse<Void> deleteSlot(@PathVariable Long id, Authentication authentication) {
        TimeSlot slot = timeSlotService.getById(id);
        if (slot == null) {
            throw new RuntimeException("未找到该排班记录");
        }
        validateCounselorAccess(slot.getCounselorId(), authentication);
        timeSlotService.deleteSlot(id);
        return ApiResponse.success(null);
    }

    private void validateCounselorAccess(Long counselorId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("未登录");
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) {
            return;
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("无效用户"));
        Counselor counselor = counselorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AccessDeniedException("当前账号不是咨询师"));

        if (!"APPROVED".equals(counselor.getStatus())) {
            throw new AccessDeniedException("咨询师状态不可用");
        }
        if (!counselorId.equals(counselor.getId())) {
            throw new AccessDeniedException("无权操作其他咨询师排班");
        }
    }
}
