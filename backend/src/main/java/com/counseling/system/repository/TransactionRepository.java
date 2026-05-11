package com.counseling.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.counseling.system.entity.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends BaseMapper<Transaction> {
    default List<Transaction> findByUserIdOrderByCreateTimeDesc(Long userId) {
        return selectList(new LambdaQueryWrapper<Transaction>()
                .eq(Transaction::getUserId, userId)
                .orderByDesc(Transaction::getCreateTime));
    }
}
