package com.example.redisjwtexample.redis.refresh.repository;

import com.example.redisjwtexample.redis.refresh.entity.RefreshTokenEntity;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, String> {
    void deleteByUserId(String userId);

//    Optional<RefreshTokenEntity> findByUserId(String userId);
}
