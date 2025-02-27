package com.example.redisjwtexample.user.service;

import com.example.redisjwtexample.user.entity.UserEntity;
import com.example.redisjwtexample.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${sample.create-user}")
    private boolean createSampleUser;

    @PostConstruct
    public void init() {
        if (createSampleUser) createDefaultUsers();
    }

    /**
     * 기본 사용자 저장 (최초 실행 시)
     */
    public void createDefaultUsers() {
        if (userRepository.count() > 0) return;

        String adminId = "admin";
        String adminPwd = "admin1234";
        String adminRole = "admin";

        UserEntity admin = new UserEntity(adminId, passwordEncoder.encode(adminPwd), adminRole);

        userRepository.save(admin);

        String userId = "user";
        String userPwd = "user1234";
        String userRole = "user";

        UserEntity user = new UserEntity(userId, passwordEncoder.encode(userPwd), userRole);

        userRepository.save(user);

        log.info(">> test 계정 생성 완료 admin ID:{}, pwd:{}", adminId, adminPwd);
        log.info(">> test 계정 생성 완료 user ID:{}, pwd:{}", userId, userPwd);
    }
}
