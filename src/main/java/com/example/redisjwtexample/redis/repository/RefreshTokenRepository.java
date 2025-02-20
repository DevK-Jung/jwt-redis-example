package com.example.redisjwtexample.redis.repository;

import com.example.redisjwtexample.redis.entity.RefreshTokenEntity;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, String> {
}
