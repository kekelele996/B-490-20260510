package com.counseling.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.counseling.system.entity.ReferralRecord;
import com.counseling.system.entity.SystemSetting;
import com.counseling.system.entity.User;
import com.counseling.system.repository.ReferralRecordRepository;
import com.counseling.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReferralService extends ServiceImpl<ReferralRecordRepository, ReferralRecord> implements IReferralService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private ISystemSettingService systemSettingService;

    @Override
    public String generateInviteCode(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getInviteCode() != null && !user.getInviteCode().isEmpty()) {
            return user.getInviteCode();
        }
        String code = generateUniqueCode();
        user.setInviteCode(code);
        userRepository.updateById(user);
        return code;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (userRepository.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getInviteCode, code)) != null);
        return code;
    }

    @Override
    public Long resolveInviterByCode(String inviteCode) {
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            return null;
        }
        User inviter = userRepository.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getInviteCode, inviteCode));
        if (inviter == null) {
            throw new RuntimeException("邀请码无效");
        }
        return inviter.getId();
    }

    @Override
    public ReferralRecord createReferralRecord(Long inviterId, Long inviteeId) {
        ReferralRecord record = new ReferralRecord();
        record.setInviterId(inviterId);
        record.setInviteeId(inviteeId);
        record.setStatus("PENDING");
        record.setRewardAmount(0.0);
        record.setCreateTime(LocalDateTime.now());
        save(record);
        return record;
    }

    @Override
    @Transactional
    public void processReferralReward(Long appointmentId, Long inviteeId) {
        User invitee = userRepository.selectById(inviteeId);
        if (invitee == null || invitee.getInviterId() == null) {
            return;
        }

        List<ReferralRecord> completedRecords = baseMapper.findByInviteeIdAndStatus(inviteeId, "COMPLETED");
        if (!completedRecords.isEmpty()) {
            return;
        }

        List<ReferralRecord> pendingRecords = baseMapper.findByInviteeIdAndStatus(inviteeId, "PENDING");
        if (pendingRecords.isEmpty()) {
            return;
        }

        ReferralRecord record = pendingRecords.get(0);
        Double rewardAmount = getReferralRewardAmount();

        record.setAppointmentId(appointmentId);
        record.setRewardAmount(rewardAmount);
        record.setStatus("COMPLETED");
        record.setCompleteTime(LocalDateTime.now());
        updateById(record);

        User inviter = userRepository.selectById(record.getInviterId());
        if (inviter != null) {
            inviter.setBalance((inviter.getBalance() == null ? 0.0 : inviter.getBalance()) + rewardAmount);
            userRepository.updateById(inviter);

            transactionService.recordTransaction(
                    inviter.getId(), rewardAmount, "REFERRAL_REWARD", "SUCCESS",
                    "邀请用户[" + invitee.getNickname() + "]首单返现");

            notificationService.createNotification(
                    inviter.getId(),
                    "您邀请的用户[" + invitee.getNickname() + "]已完成首单付费预约，返现" + rewardAmount + "元已到账",
                    "REFERRAL_REWARD",
                    record.getId());
        }
    }

    @Override
    public List<ReferralRecord> getMyReferrals(Long inviterId) {
        return baseMapper.findByInviterId(inviterId);
    }

    @Override
    public Double getReferralRewardAmount() {
        SystemSetting setting = systemSettingService.getByKey("referral_reward_amount");
        if (setting != null && setting.getSettingValue() != null) {
            try {
                return Double.parseDouble(setting.getSettingValue());
            } catch (NumberFormatException e) {
                return 10.0;
            }
        }
        return 10.0;
    }
}
