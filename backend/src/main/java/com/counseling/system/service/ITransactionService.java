package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.counseling.system.entity.Transaction;
import java.util.List;

public interface ITransactionService extends IService<Transaction> {
    List<Transaction> getTransactionsByUserId(Long userId);
    void recordTransaction(Long userId, Double amount, String type, String status, String description);
}
