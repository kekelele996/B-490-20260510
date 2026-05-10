package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.counseling.system.entity.Transaction;
import com.counseling.system.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService extends ServiceImpl<TransactionRepository, Transaction> implements ITransactionService {
    @Override
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return baseMapper.findByUserIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public void recordTransaction(Long userId, Double amount, String type, String status, String description) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus(status);
        transaction.setDescription(description);
        transaction.setCreateTime(LocalDateTime.now());
        save(transaction);
    }
}
