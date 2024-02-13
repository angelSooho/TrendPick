package project.trendpick_pro.global.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void enqueue(String requestId) {
        redisTemplate.opsForList().rightPush("requestQueue", requestId);
    }

    public String dequeue(Long maxWaitRequest) {
        Long currentSize = redisTemplate.opsForList().size("requestQueue");
        try {
            if (currentSize <= maxWaitRequest) {
                return redisTemplate.opsForList().leftPop("requestQueue", 10, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
