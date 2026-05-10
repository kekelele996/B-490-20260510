package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.counseling.system.entity.TimeSlot;
import java.util.List;

public interface ITimeSlotService extends IService<TimeSlot> {
    List<TimeSlot> getSlotsByCounselor(Long counselorId);
    List<TimeSlot> getAvailableSlotsByCounselor(Long counselorId);
    TimeSlot addSlot(Long counselorId, TimeSlot slot);
    List<TimeSlot> batchAddSlots(Long counselorId, List<TimeSlot> slots);
    void deleteSlot(Long id);
}
