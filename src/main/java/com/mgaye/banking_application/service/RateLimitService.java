package com.mgaye.banking_application.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isBlocked(String ipAddress) {
        String key = "rate_limit:" + ipAddress;
        String attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && Integer.parseInt(attempts) >= 10;
    }

    public void recordFailedAttempt(String ipAddress) {
        String key = "rate_limit:" + ipAddress;
        String attempts = redisTemplate.opsForValue().get(key);

        if (attempts == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofHours(1));
        } else {
            int count = Integer.parseInt(attempts) + 1;
            redisTemplate.opsForValue().set(key, String.valueOf(count), Duration.ofHours(1));
        }
    }

    public void clearAttempts(String ipAddress) {
        String key = "rate_limit:" + ipAddress;
        redisTemplate.delete(key);
    }
}