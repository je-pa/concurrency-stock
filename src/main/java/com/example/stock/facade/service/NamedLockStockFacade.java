package com.example.stock.facade.service;

import com.example.stock.repository.LockRepository;
import com.example.stock.service.NamedLockStockService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 실제 로직 전후로 락 획득 해제를 해줘야 되기 때문에 퍼싸드
 */
@Component
@RequiredArgsConstructor
public class NamedLockStockFacade {

  private final LockRepository lockRepository;

  private final NamedLockStockService namedLockStockService;

  @Transactional
  public void decrease(Long id, Long quantity) {
    try {
      //  락 레포지토리를 활용해서 락을 획득
      lockRepository.getLock(id.toString());
      // 락 획득을 하였다면 스탁 서비스를 사용해서 재고를 감소
      namedLockStockService.decrease(id, quantity);
    } finally {
      // 모든 로직이 종료가 되었을 때 락을 해제
      lockRepository.releaseLock(id.toString());
    }
  }
}