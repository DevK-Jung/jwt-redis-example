package com.example.redisjwtexample.redis.repository;

import com.example.redisjwtexample.redis.entity.RefreshTokenEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, String> {
    void deleteByUserId(String userId);

//    Optional<RefreshTokenEntity> findByUserId(String userId);
}
