package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.counseling.system.entity.WithdrawalRecord;
import java.util.List;

public interface IWithdrawalService extends IService<WithdrawalRecord> {
    List<WithdrawalRecord> getRecordsByCounselor(Long counselorId);
    List<WithdrawalRecord> getAllRecords();
    WithdrawalRecord requestWithdrawal(Long counselorId, Double amount);
    WithdrawalRecord approveWithdrawal(Long recordId);
    WithdrawalRecord rejectWithdrawal(Long recordId, String reason);
}
