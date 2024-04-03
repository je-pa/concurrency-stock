package com.zb.tablereservation.stock.service;

import com.zb.tablereservation.stock.domain.Stock;
import com.zb.tablereservation.stock.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

  private final StockRepository stockRepository;


  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void decrease(Long id, Long quantity) {
    // Stock 조회
    // 재고를 감소
    // 갱신된 값을 저장
    Stock stock = stockRepository.findById(id).orElseThrow();

    stock.decrease(quantity);

    stockRepository.saveAndFlush(stock);
  }
}