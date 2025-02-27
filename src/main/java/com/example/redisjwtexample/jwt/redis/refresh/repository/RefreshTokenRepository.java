package com.example.redisjwtexample.jwt.redis.refresh.repository;

import com.example.redisjwtexample.jwt.redis.refresh.entity.RefreshTokenEntity;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, String> {

}
