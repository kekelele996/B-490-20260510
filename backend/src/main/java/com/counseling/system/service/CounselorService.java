package com.counseling.system.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.counseling.system.entity.Counselor;
import com.counseling.system.entity.User;
import com.counseling.system.repository.CounselorRepository;
import com.counseling.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CounselorService extends ServiceImpl<CounselorRepository, Counselor> implements ICounselorService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Counselor> getAllCounselors() {
        List<Counselor> counselors = list();
        for (Counselor c : counselors) {
            if (c.getUserId() != null) {
                User user = userRepository.selectById(c.getUserId());
                c.setUser(user);
            }
        }
        return counselors;
    }
    
    // Original getCounselorById is replaced by getById from ServiceImpl for internal use
    // and a new getByUserId is introduced.
    // The original getCounselorById was:
    // public Counselor getCounselorById(Long id) {
    //     return counselorRepository.findById(id).orElseThrow(() -> new RuntimeException("未找到该咨询师"));
    // }

    @Override
    public Counselor getByUserId(Long userId) {
        return baseMapper.findByUserId(userId).orElse(null);
    }

    @Override
    @Transactional
    public Counselor applyForCounselor(Long userId, Counselor counselorDetails) {
        User user = userRepository.selectById(userId);
        if (user == null) throw new RuntimeException("未找到该用户");

        if (counselorDetails.getQualification() == null || counselorDetails.getQualification().trim().isEmpty()) {
            throw new RuntimeException("请上传资质证明");
        }

        Counselor existing = baseMapper.findByUserId(userId).orElse(null);
        if (existing != null) {
            if ("APPROVED".equals(existing.getStatus()) || "COUNSELOR".equals(user.getRole())) {
                throw new RuntimeException("您已是咨询师，无需重复申请");
            }
            mergeCounselorProfile(existing, counselorDetails);
            existing.setStatus("APPROVED");
            updateById(existing);
            promoteToCounselor(user);
            return existing;
        }

        Counselor counselor = new Counselor();
        counselor.setUserId(userId);
        mergeCounselorProfile(counselor, counselorDetails);
        counselor.setStatus("APPROVED");
        counselor.setTotalRevenue(0.0);
        save(counselor);
        promoteToCounselor(user);
        return counselor;
    }
    
    @Override
    @Transactional
    public Counselor approveCounselor(Long id) {
        Counselor counselor = getById(id);
        if (counselor == null) throw new RuntimeException("未找到该咨询师");
        counselor.setStatus("APPROVED");
        User user = userRepository.selectById(counselor.getUserId());
        if (user != null) {
            user.setRole("COUNSELOR");
            userRepository.updateById(user);
        }
        updateById(counselor);
        return counselor;
    }
    
    @Override
    @Transactional
    public Counselor rejectCounselor(Long id) {
        Counselor counselor = getById(id);
        if (counselor == null) throw new RuntimeException("未找到该咨询师");
        counselor.setStatus("REJECTED");
        User user = userRepository.selectById(counselor.getUserId());
        if (user != null) {
            user.setRole("USER");
            userRepository.updateById(user);
        }
        updateById(counselor);
        return counselor;
    }

    public Counselor updateCounselor(Long id, Counselor counselorData) {
        Counselor counselor = getById(id);
        if (counselor == null) throw new RuntimeException("未找到该咨询师");
        if (counselorData.getIntroduction() != null) counselor.setIntroduction(counselorData.getIntroduction());
        if (counselorData.getExpertise() != null) counselor.setExpertise(counselorData.getExpertise());
        if (counselorData.getFee() != null) counselor.setFee(counselorData.getFee());
        if (counselorData.getAvailableTime() != null) counselor.setAvailableTime(counselorData.getAvailableTime());
        if (counselorData.getQualification() != null) counselor.setQualification(counselorData.getQualification());
        updateById(counselor);
        return counselor;
    }
    
    public Double withdrawRevenue(Long id) {
        Counselor counselor = getById(id);
        if (counselor == null) throw new RuntimeException("未找到该咨询师");
        Double amount = counselor.getTotalRevenue();
        counselor.setTotalRevenue(0.0);
        updateById(counselor);
        return amount;
    }
    
    @Override
    @Transactional
    public void deleteCounselor(Long id) {
        Counselor counselor = getById(id);
        if (counselor == null) {
            throw new RuntimeException("未找到该咨询师");
        }
        User user = userRepository.selectById(counselor.getUserId());
        if (user != null) {
            user.setRole("USER");
            userRepository.updateById(user);
        }
        removeById(id);
    }

    private void mergeCounselorProfile(Counselor target, Counselor source) {
        target.setIntroduction(source.getIntroduction());
        target.setExpertise(source.getExpertise());
        target.setFee(source.getFee());
        target.setQualification(source.getQualification());
        target.setAvailableTime(source.getAvailableTime());
    }

    private void promoteToCounselor(User user) {
        if (!"COUNSELOR".equals(user.getRole())) {
            user.setRole("COUNSELOR");
            userRepository.updateById(user);
        }
    }
}
