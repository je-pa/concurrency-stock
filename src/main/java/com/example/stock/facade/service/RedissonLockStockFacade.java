package com.example.stock.facade.service;

import com.example.stock.service.StockService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * 로직 실행 전후로 락 획득 해제를 해주는 퍼사드 클래스
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedissonLockStockFacade {
  // 락 획득에 사용할 레디슨 클라이언트
  private final RedissonClient redissonClient;

  private final StockService stockService;
  public void decrease(Long key, Long quantity) {
    // lock 객체
    RLock lock = redissonClient.getLock(key.toString());

    try {
      // 몇 초 동안 락 획득을 시도할 것인지, 몇 초 동안 점유할 것인지를 설정
      boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);

      if (!available) {
        log.info("lock 획득 실패");
        return;
      }

      stockService.decrease(key, quantity);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }
}