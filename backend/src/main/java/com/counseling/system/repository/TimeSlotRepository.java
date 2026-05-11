package com.counseling.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.counseling.system.entity.TimeSlot;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotRepository extends BaseMapper<TimeSlot> {
    default List<TimeSlot> findByCounselorIdOrderByStartTime(Long counselorId) {
        return selectList(new LambdaQueryWrapper<TimeSlot>()
                .eq(TimeSlot::getCounselorId, counselorId)
                .orderByAsc(TimeSlot::getStartTime));
    }

    default List<TimeSlot> findByCounselorIdAndStatusOrderByStartTime(Long counselorId, String status) {
        return selectList(new LambdaQueryWrapper<TimeSlot>()
                .eq(TimeSlot::getCounselorId, counselorId)
                .eq(TimeSlot::getStatus, status)
                .orderByAsc(TimeSlot::getStartTime));
    }
}
