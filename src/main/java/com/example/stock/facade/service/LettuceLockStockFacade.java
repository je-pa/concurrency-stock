package com.example.stock.facade.service;

import com.example.stock.repository.RedisLockRepository;
import com.example.stock.service.NamedLockStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 로직 실행 전후로 락 획득 해제를 수행하기위한 facade
 */
@Component
@RequiredArgsConstructor
public class LettuceLockStockFacade {

  private final RedisLockRepository redisLockRepository;

  private final NamedLockStockService stockService;

  public void decrease(Long key, Long quantity) throws InterruptedException {
    while (!redisLockRepository.lock(key)) { // lock 획득 시도
      Thread.sleep(100); // 레디스 부하 줄임
    }

    try {
      stockService.decrease(key, quantity);
    } finally {
      redisLockRepository.unlock(key); // lock 해제
    }
  }
}