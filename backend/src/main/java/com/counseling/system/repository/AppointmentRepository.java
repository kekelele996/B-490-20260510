package com.counseling.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.counseling.system.entity.Appointment;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends BaseMapper<Appointment> {
    default List<Appointment> findByUserId(Long userId) {
        return selectList(new LambdaQueryWrapper<Appointment>().eq(Appointment::getUserId, userId));
    }

    default List<Appointment> findByCounselorId(Long counselorId) {
        return selectList(new LambdaQueryWrapper<Appointment>().eq(Appointment::getCounselorId, counselorId));
    }
}
