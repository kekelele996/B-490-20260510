package com.counseling.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.counseling.system.entity.InviteRelation;
import com.counseling.system.entity.SystemSetting;
import com.counseling.system.entity.User;
import com.counseling.system.repository.InviteRelationRepository;
import com.counseling.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class InviteService extends ServiceImpl<InviteRelationRepository, InviteRelation> implements IInviteService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ISystemSettingService systemSettingService;

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private INotificationService notificationService;

    private static final String INVITE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 8;

    @Override
    public String generateInviteCode(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getInviteCode() != null && !user.getInviteCode().isEmpty()) {
            return user.getInviteCode();
        }

        String inviteCode = generateUniqueCode();
        user.setInviteCode(inviteCode);
        userRepository.updateById(user);

        return inviteCode;
    }

    private String generateUniqueCode() {
        Random random = new Random();
        String code;
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < INVITE_CODE_LENGTH; j++) {
                sb.append(INVITE_CODE_CHARS.charAt(random.nextInt(INVITE_CODE_CHARS.length())));
            }
            code = sb.toString();
            User existing = userRepository.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getInviteCode, code));
            if (existing == null) {
                return code;
            }
        }
        throw new RuntimeException("生成邀请码失败，请稍后重试");
    }

    @Override
    public void bindInviteRelation(Long inviteeId, String inviteCode) {
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            return;
        }

        User inviter = userRepository.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getInviteCode, inviteCode.trim().toUpperCase()));
        if (inviter == null) {
            throw new RuntimeException("邀请码无效");
        }

        if (inviter.getId().equals(inviteeId)) {
            throw new RuntimeException("不能使用自己的邀请码");
        }

        User invitee = userRepository.selectById(inviteeId);
        if (invitee.getInviterId() != null) {
            throw new RuntimeException("已绑定邀请关系，无法重复绑定");
        }

        invitee.setInviterId(inviter.getId());
        userRepository.updateById(invitee);

        InviteRelation relation = new InviteRelation();
        relation.setInviterId(inviter.getId());
        relation.setInviteeId(inviteeId);
        relation.setCreateTime(LocalDateTime.now());
        relation.setRewardGiven(false);
        save(relation);

        notificationService.createNotification(
            inviter.getId(),
            "恭喜！您邀请的用户 " + invitee.getNickname() + " 已成功注册",
            "INVITE",
            inviteeId
        );
    }

    @Override
    public void processFirstOrderReward(Long inviteeId) {
        User invitee = userRepository.selectById(inviteeId);
        if (invitee == null || invitee.getFirstOrderCompleted()) {
            return;
        }

        InviteRelation relation = baseMapper.findByInviteeId(inviteeId).orElse(null);
        if (relation == null || relation.getRewardGiven()) {
            invitee.setFirstOrderCompleted(true);
            userRepository.updateById(invitee);
            return;
        }

        SystemSetting setting = systemSettingService.getByKey("invite_reward_amount");
        Double rewardAmount = setting != null && setting.getSettingValue() != null
            ? Double.parseDouble(setting.getSettingValue())
            : 50.0;

        User inviter = userRepository.selectById(relation.getInviterId());
        if (inviter != null) {
            inviter.setBalance((inviter.getBalance() == null ? 0.0 : inviter.getBalance()) + rewardAmount);
            userRepository.updateById(inviter);

            transactionService.recordTransaction(
                inviter.getId(),
                rewardAmount,
                "REFERRAL_REWARD",
                "SUCCESS",
                "邀请奖励 - 用户 " + invitee.getNickname() + " 完成首单"
            );

            notificationService.createNotification(
                inviter.getId(),
                "恭喜！您邀请的用户 " + invitee.getNickname() + " 完成首单，获得 " + rewardAmount + " 元奖励",
                "WALLET",
                relation.getId()
            );
        }

        relation.setRewardGiven(true);
        relation.setRewardAmount(rewardAmount);
        relation.setRewardTime(LocalDateTime.now());
        updateById(relation);

        invitee.setFirstOrderCompleted(true);
        userRepository.updateById(invitee);
    }

    @Override
    public List<InviteRelation> getMyInviteList(Long inviterId) {
        List<InviteRelation> list = baseMapper.findByInviterId(inviterId);
        for (InviteRelation relation : list) {
            if (relation.getInviteeId() != null) {
                relation.setInvitee(userRepository.selectById(relation.getInviteeId()));
            }
        }
        return list;
    }

    @Override
    public InviteRelation getInviteRelationByInviteeId(Long inviteeId) {
        return baseMapper.findByInviteeId(inviteeId).orElse(null);
    }
}
