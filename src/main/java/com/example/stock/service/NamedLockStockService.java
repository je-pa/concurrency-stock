package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NamedLockStockService {

  private final StockRepository stockRepository;


  //스탁 서비스에서는 부모의 트랜지션과 별도로 실행이 되어야 되기 때문에 propagation을 변경
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void decrease(Long id, Long quantity) {
    Stock stock = stockRepository.findById(id).orElseThrow();

    stock.decrease(quantity);

    stockRepository.saveAndFlush(stock);
  }


}