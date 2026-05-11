package com.counseling.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.counseling.system.entity.WithdrawalRecord;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WithdrawalRecordRepository extends BaseMapper<WithdrawalRecord> {
}
