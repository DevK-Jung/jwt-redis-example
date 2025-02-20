package com.example.redisjwtexample.user.repository;

import com.example.redisjwtexample.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
