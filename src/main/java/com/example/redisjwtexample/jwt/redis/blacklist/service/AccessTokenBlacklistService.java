package com.example.redisjwtexample.jwt.redis.blacklist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AccessTokenBlacklistService {

    private final RedisTemplate<String, String> redisBlackListTemplate;

    // 블랙리스트에 추가 (key: "blacklist:token:{accessToken}", value: "invalid")
    public void setBlackList(String accessToken, Long expiryMillis) {
        redisBlackListTemplate.opsForValue().set(getKey(accessToken), "invalid", expiryMillis, TimeUnit.MILLISECONDS);
    }

    // 블랙리스트 조회
    public boolean isBlacklisted(String accessToken) {
        return redisBlackListTemplate.hasKey(getKey(accessToken));
    }

    // 블랙리스트에서 삭제 (필요한 경우)
    public boolean removeBlackList(String accessToken) {
        return Boolean.TRUE.equals(redisBlackListTemplate.delete(getKey(accessToken)));
    }


    // Key 생성 메서드
    private String getKey(String accessToken) {
        return "blacklist:token:" + accessToken;
    }
}
