package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.counseling.system.entity.Counselor;
import com.counseling.system.entity.WithdrawalRecord;
import com.counseling.system.repository.CounselorRepository;
import com.counseling.system.repository.WithdrawalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WithdrawalService extends ServiceImpl<WithdrawalRecordRepository, WithdrawalRecord> implements IWithdrawalService {

    @Autowired
    private CounselorRepository counselorRepository;

    @Autowired
    private WithdrawalRecordRepository withdrawalRecordRepository;

    @Autowired
    private com.counseling.system.repository.UserRepository userRepository;

    @Override
    public List<WithdrawalRecord> getRecordsByCounselor(Long counselorId) {
        return lambdaQuery().eq(WithdrawalRecord::getCounselorId, counselorId).list();
    }

    @Override
    public List<WithdrawalRecord> getAllRecords() {
        List<WithdrawalRecord> records = list();
        for (WithdrawalRecord record : records) {
             if (record.getCounselorId() != null) {
                 Counselor c = counselorRepository.selectById(record.getCounselorId());
                 if (c != null && c.getUserId() != null) {
                     c.setUser(userRepository.selectById(c.getUserId()));
                 }
                 record.setCounselor(c);
             }
        }
        return records;
    }

    @Override
    public WithdrawalRecord requestWithdrawal(Long counselorId, Double amount) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("提现金额必须大于0");
        }
        Counselor counselor = counselorRepository.selectById(counselorId);
        if (counselor == null) throw new RuntimeException("未找到该咨询师");

        Double totalRevenue = counselor.getTotalRevenue() == null ? 0.0 : counselor.getTotalRevenue();
        if (totalRevenue < amount) {
            throw new RuntimeException("余额不足");
        }

        counselor.setTotalRevenue(totalRevenue - amount);
        counselorRepository.updateById(counselor);

        WithdrawalRecord record = new WithdrawalRecord();
        record.setCounselor(counselor);
        record.setCounselorId(counselor.getId());
        record.setAmount(amount);
        record.setRequestTime(LocalDateTime.now());
        record.setStatus("PENDING");
        save(record);
        return record;
    }

    @Override
    public WithdrawalRecord approveWithdrawal(Long recordId) {
        WithdrawalRecord record = getById(recordId);
        if (record == null) throw new RuntimeException("未找到该提现记录");
        
        if (!"PENDING".equals(record.getStatus())) {
            throw new RuntimeException("该记录不处于待处理状态");
        }

        record.setStatus("APPROVED");
        updateById(record);
        return record;
    }

    @Override
    public WithdrawalRecord rejectWithdrawal(Long recordId, String reason) {
        WithdrawalRecord record = getById(recordId);
        if (record == null) throw new RuntimeException("未找到该提现记录");

        if (!"PENDING".equals(record.getStatus())) {
            throw new RuntimeException("该记录不处于待处理状态");
        }

        record.setStatus("REJECTED");
        record.setNote(reason);

        Counselor counselor = record.getCounselor();
        if (counselor == null && record.getCounselorId() != null) {
            counselor = counselorRepository.selectById(record.getCounselorId());
        }
        if (counselor == null) {
            throw new RuntimeException("关联咨询师不存在");
        }
        counselor.setTotalRevenue((counselor.getTotalRevenue() == null ? 0.0 : counselor.getTotalRevenue()) + record.getAmount());
        counselorRepository.updateById(counselor);

        updateById(record);
        return record;
    }
}
