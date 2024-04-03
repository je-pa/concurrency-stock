package com.example.stock.facade.service;

import com.example.stock.service.OptimisticLockStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * optimistic-lock은 실패했을 때 재시도 를 해야해서 생성
 */
@Component
@RequiredArgsConstructor
public class OptimisticLockStockFacade {

  private final OptimisticLockStockService optimisticLockStockService;

  public void decrease(Long id, Long quantity) throws InterruptedException {
    while (true) {
      try {
        optimisticLockStockService.decrease(id, quantity);

        break;
      } catch (Exception e) {
        Thread.sleep(50);
      }
    }
  }
}