package project.trendpick_pro.global.redis.redisson.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import project.trendpick_pro.domain.orders.service.OrderService;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedissonService {

    private final RedissonClient redissonClient;
    private final OrderService orderService;

    public void processOrderWithLock(String orderKey) throws InterruptedException {
        RLock lock = getDistributedLock(orderKey);
        if (lock.tryLock(3, 3, TimeUnit.SECONDS)) {
            try {
                log.info(lock.getName());
                orderService.tryOrder(orderKey);
            } catch (JsonProcessingException e) {
                log.error("Error processing order", e);
            } finally {;
                try {
                    lock.unlock();
                } catch (IllegalMonitorStateException e) {
                    log.info("Redisson Lock Already UnLocked");
                }
            }
        } else {
            throw new BaseException(ErrorCode.BAD_REQUEST, "주문 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private RLock getDistributedLock(String key) {
        return redissonClient.getLock("ORD_" + key);
    }
}
