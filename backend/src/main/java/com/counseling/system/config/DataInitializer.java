package com.counseling.system.config;

import com.counseling.system.entity.*;
import com.counseling.system.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CounselorRepository counselorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private WithdrawalRecordRepository withdrawalRecordRepository;

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override

    public void run(String... args) throws Exception {
        if (userRepository.selectCount(null) > 0) {
            return; // Data already exists
        }

        // 1. Create Admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));

        admin.setNickname("System Admin");
        admin.setRole("ADMIN");
        admin.setPhone("13800000000");
        admin.setEmail("admin@example.com");
        admin.setGender("Male");
        userRepository.insert(admin);

        // 2. Create Counselor User
        User counselorUser = new User();
        counselorUser.setUsername("counselor1");
        counselorUser.setPassword(passwordEncoder.encode("123456"));

        counselorUser.setNickname("Dr. Zhang");
        counselorUser.setRole("COUNSELOR");
        counselorUser.setPhone("13900000000");
        counselorUser.setEmail("zhang@example.com");
        counselorUser.setGender("Male");
        userRepository.insert(counselorUser);

        // 3. Create Counselor Profile
        Counselor counselor = new Counselor();
        counselor.setUserId(counselorUser.getId());
        counselor.setIntroduction("Experienced psychologist with 10 years of practice.");
        counselor.setExpertise("Anxiety, Depression");
        counselor.setFee(500.0);
        counselor.setStatus("APPROVED");
        counselor.setAvailableTime("Mon-Fri 09:00-17:00");
        counselor.setTotalRevenue(1000.0);
        counselorRepository.insert(counselor);
        
        // 4. Create Another Counselor (Pending)
        User counselorUser2 = new User();
        counselorUser2.setUsername("counselor2");
        counselorUser2.setPassword(passwordEncoder.encode("123456"));

        counselorUser2.setNickname("Li Psychologist");
        counselorUser2.setRole("COUNSELOR");
        counselorUser2.setPhone("13600000000");
        counselorUser2.setEmail("li@example.com");
        counselorUser2.setGender("Female");
        userRepository.insert(counselorUser2);
        
        Counselor counselor2 = new Counselor();
        counselor2.setUserId(counselorUser2.getId());
        counselor2.setIntroduction("Specializing in child psychology.");
        counselor2.setExpertise("Child, Family");
        counselor2.setFee(300.0);
        // Use a placeholder image for testing qualification view
        counselor2.setQualification("https://dummyimage.com/600x400/000/fff&text=Qualification+Proof");
        counselor2.setStatus("APPROVED");
        counselorRepository.insert(counselor2);

        // 5. Create Normal User
        User normalUser = new User();
        normalUser.setUsername("user1");
        normalUser.setPassword(passwordEncoder.encode("123456"));

        normalUser.setNickname("Xiao Wang");
        normalUser.setRole("USER");
        normalUser.setPhone("13700000000");
        normalUser.setEmail("wang@example.com");
        normalUser.setGender("Female");
        userRepository.insert(normalUser);

        // Add another user for data richness
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword(passwordEncoder.encode("123456"));

        user2.setNickname("Xiao Li");
        user2.setRole("USER");
        user2.setPhone("13500000000");
        user2.setEmail("li_user@example.com");
        user2.setGender("Male");
        userRepository.insert(user2);

        // 6. Create Appointments
        Appointment appt1 = new Appointment();
        appt1.setUserId(normalUser.getId());
        appt1.setCounselorId(counselor.getId());
        appt1.setAppointmentTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        appt1.setStatus("PENDING");
        appointmentRepository.insert(appt1);

        Appointment appt2 = new Appointment();
        appt2.setUserId(normalUser.getId());
        appt2.setCounselorId(counselor.getId());
        appt2.setAppointmentTime(LocalDateTime.now().minusDays(1).withHour(14).withMinute(0));
        appt2.setStatus("COMPLETED");
        appt2.setRating(5);
        appt2.setFeedback("Very helpful session!");
        appointmentRepository.insert(appt2);
        
        // Add another appointment for user2
        Appointment appt3 = new Appointment();
        appt3.setUserId(user2.getId());
        appt3.setCounselorId(counselor.getId()); // Same counselor
        appt3.setAppointmentTime(LocalDateTime.now().plusDays(2).withHour(15).withMinute(0));
        appt3.setStatus("CONFIRMED");
        appointmentRepository.insert(appt3);

        // 7. Create Withdrawal
        WithdrawalRecord withdrawal = new WithdrawalRecord();
        withdrawal.setCounselorId(counselor.getId());
        withdrawal.setAmount(200.0);
        withdrawal.setRequestTime(LocalDateTime.now().minusHours(2));
        withdrawal.setStatus("PENDING");
        withdrawalRecordRepository.insert(withdrawal);
        
        SystemSetting referralReward = new SystemSetting();
        referralReward.setSettingKey("referral_reward_amount");
        referralReward.setSettingValue("10.0");
        referralReward.setDescription("老带新邀请返现金额");
        systemSettingRepository.insert(referralReward);

        log.info("Dummy data initialized.");
    }
}
