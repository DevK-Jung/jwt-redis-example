package com.example.redisjwtexample.user.service;

import com.example.redisjwtexample.user.entity.UserEntity;
import com.example.redisjwtexample.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostConstruct
    public void init() {
        createDefaultUsers();
    }

    /**
     * 기본 사용자 저장 (최초 실행 시)
     */
    public void createDefaultUsers() {
        if (userRepository.count() > 0) return;
        UserEntity admin = new UserEntity("admin", passwordEncoder.encode("admin1234"));

        userRepository.save(admin);

        UserEntity user = new UserEntity("user", passwordEncoder.encode("user1234"));

        userRepository.save(user);

        log.debug(">> test 계정 생성 완료 admin ID:{}, pwd:{}", admin.getUserId(), "admin1234");
        log.debug(">> test 계정 생성 완료 user ID:{}, pwd:{}", user.getId(), "user1234");
    }
}
