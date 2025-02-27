package com.example.redisjwtexample.redis;

import com.example.redisjwtexample.jwt.redis.refresh.entity.RefreshTokenEntity;
import com.example.redisjwtexample.jwt.redis.refresh.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataRedisTest  // Spring Boot Redis 테스트 환경
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();  // 테스트 전 데이터 삭제
    }

    @Test
    void testSaveAndFindRefreshToken() {
        // Given: 테스트 데이터 생성
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity("user123", "sample-refresh-token", 3600L);

        // When: Redis에 저장
        refreshTokenRepository.save(refreshTokenEntity);

        // Then: 저장된 데이터 확인
        Optional<RefreshTokenEntity> foundEntity = refreshTokenRepository.findById("user123");
        assertTrue(foundEntity.isPresent());
        assertEquals("sample-refresh-token", foundEntity.get().getRefreshToken());
    }

    @Test
    void testRefreshTokenExpiration() throws InterruptedException {
        // Given: TTL이 2초인 Refresh Token 저장
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity("user123", "sample-refresh-token", 2L);
        refreshTokenRepository.save(refreshTokenEntity);

        // When: 3초 후 데이터 조회 (만료 확인)
        Thread.sleep(3000);
        Optional<RefreshTokenEntity> foundEntity = refreshTokenRepository.findById("user123");

        // Then: TTL이 만료되어 데이터가 없어야 함
        assertFalse(foundEntity.isPresent());
    }

    @Test
    void testDeleteRefreshToken() {
        // Given: 데이터 저장
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity("user123", "sample-refresh-token", 3600L);
        refreshTokenRepository.save(refreshTokenEntity);

        // When: 삭제 수행
        refreshTokenRepository.deleteById("user123");

        // Then: 데이터가 존재하지 않아야 함
        Optional<RefreshTokenEntity> foundEntity = refreshTokenRepository.findById("user123");
        assertFalse(foundEntity.isPresent());
    }
}
