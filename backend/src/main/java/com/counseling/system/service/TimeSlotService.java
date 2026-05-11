package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.counseling.system.entity.Counselor;
import com.counseling.system.entity.TimeSlot;
import com.counseling.system.repository.CounselorRepository;
import com.counseling.system.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimeSlotService extends ServiceImpl<TimeSlotRepository, TimeSlot> implements ITimeSlotService {

    @Autowired
    private CounselorRepository counselorRepository;

    @Override
    public List<TimeSlot> getSlotsByCounselor(Long counselorId) {
        return baseMapper.findByCounselorIdOrderByStartTime(counselorId);
    }
    
    @Override
    public List<TimeSlot> getAvailableSlotsByCounselor(Long counselorId) {
        return baseMapper.findByCounselorIdAndStatusOrderByStartTime(counselorId, "AVAILABLE");
    }

    @Override
    public TimeSlot addSlot(Long counselorId, TimeSlot slot) {
        Counselor counselor = counselorRepository.selectById(counselorId);
        if (counselor == null) throw new RuntimeException("未找到该咨询师");
        slot.setCounselor(counselor);
        slot.setCounselorId(counselorId);
        slot.setStatus("AVAILABLE");
        save(slot);
        return slot;
    }

    @Override
    public List<TimeSlot> batchAddSlots(Long counselorId, List<TimeSlot> slots) {
        Counselor counselor = counselorRepository.selectById(counselorId);
        if (counselor == null) throw new RuntimeException("未找到该咨询师");
        for (TimeSlot slot : slots) {
            slot.setCounselor(counselor);
            slot.setCounselorId(counselorId);
            slot.setStatus("AVAILABLE");
        }
        saveBatch(slots);
        return slots;
    }

    @Override
    public void deleteSlot(Long id) {
        removeById(id);
    }
}

