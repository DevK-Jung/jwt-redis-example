package com.example.redisjwtexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableRedisRepositories
public class RedisJwtExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisJwtExampleApplication.class, args);
    }

}
