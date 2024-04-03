package com.example.stock.facade.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OptimisticLockStockFacadeTest {
  @Autowired
  private OptimisticLockStockFacade optimisticLockStockFacade;

  @Autowired
  private StockRepository stockRepository;

  @BeforeEach
  public void insert() {
    Stock stock = new Stock(1L, 100L);

    stockRepository.saveAndFlush(stock);
  }

  @AfterEach
  public void delete() {
    stockRepository.deleteAll();
  }
  @Test
  public void 동시에_100명이_주문() throws InterruptedException {
    int threadCount = 100;
    // 익스큐터 서비스는 비동기로 실행하는 작업을 단순화하여 사용할 수 있게 도와주는 Java의 API
    ExecutorService executorService = Executors.newFixedThreadPool(32);
    // 100개의 요청이 모두 끝날 때까지 기다려야 하므로 카운트다운 레치를 활용
    // CountDownLatch는 다른 Thread에서 수행 중인 작업이 완료될 때까지 대기할 수 있도록 도와주는 클래스
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          optimisticLockStockFacade.decrease(1L, 1L);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    Stock stock = stockRepository.findById(1L).orElseThrow();

    // 100 - (100 * 1) = 0
    assertEquals(0, stock.getQuantity());
  }
}