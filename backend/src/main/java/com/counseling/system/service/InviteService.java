package com.counseling.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.counseling.system.entity.InviteRecord;
import com.counseling.system.entity.SystemSetting;
import com.counseling.system.entity.Transaction;
import com.counseling.system.entity.User;
import com.counseling.system.repository.InviteRecordRepository;
import com.counseling.system.repository.SystemSettingRepository;
import com.counseling.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InviteService extends ServiceImpl<InviteRecordRepository, InviteRecord> implements IInviteService {

    private static final String INVITE_REBATE_AMOUNT_KEY = "invite_rebate_amount";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final double DEFAULT_REBATE_AMOUNT = 50.0;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public String generateInviteCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    @Override
    public User bindInviteCode(User invitee, String inviteCode) {
        return invitee;
    }

    @Override
    public void processInviteRebate(Long inviteeId) {
        InviteRecord record = baseMapper.findByInviteeId(inviteeId).orElse(null);
        if (record == null || !"PENDING".equals(record.getStatus())) {
            return;
        }

        User inviter = userRepository.selectById(record.getInviterId());
        if (inviter == null) {
            return;
        }

        Double rebateAmount = getRebateAmount();
        if (rebateAmount <= 0) {
            return;
        }

        inviter.setBalance((inviter.getBalance() == null ? 0.0 : inviter.getBalance()) + rebateAmount);
        userRepository.updateById(inviter);

        Transaction transaction = new Transaction();
        transaction.setUserId(inviter.getId());
        transaction.setAmount(rebateAmount);
        transaction.setType("REBATE");
        transaction.setStatus("SUCCESS");
        transaction.setCreateTime(LocalDateTime.now());
        transaction.setDescription("邀请好友返现");
        transactionService.save(transaction);

        record.setStatus("COMPLETED");
        record.setRebateAmount(rebateAmount);
        record.setCompleteTime(LocalDateTime.now());
        updateById(record);

        notificationService.createNotification(
            inviter.getId(),
            "恭喜！您邀请的好友已完成首次付费预约，获得返现 ¥" + rebateAmount,
            "REBATE",
            record.getId()
        );
    }

    @Override
    public List<InviteRecord> getMyInvites(Long inviterId) {
        List<InviteRecord> records = baseMapper.findByInviterId(inviterId);
        for (InviteRecord record : records) {
            User invitee = userRepository.selectById(record.getInviteeId());
            record.setInvitee(invitee);
        }
        return records;
    }

    @Override
    public InviteRecord getMyInviterInfo(Long inviteeId) {
        Optional<InviteRecord> recordOpt = baseMapper.findByInviteeId(inviteeId);
        if (!recordOpt.isPresent()) {
            return null;
        }
        InviteRecord record = recordOpt.get();
        User inviter = userRepository.selectById(record.getInviterId());
        record.setInviter(inviter);
        return record;
    }

    private Double getRebateAmount() {
        SystemSetting setting = systemSettingRepository.findBySettingKeyOptional(INVITE_REBATE_AMOUNT_KEY).orElse(null);
        if (setting == null || setting.getSettingValue() == null || setting.getSettingValue().trim().isEmpty()) {
            return DEFAULT_REBATE_AMOUNT;
        }
        try {
            return Double.parseDouble(setting.getSettingValue().trim());
        } catch (NumberFormatException e) {
            return DEFAULT_REBATE_AMOUNT;
        }
    }
}
