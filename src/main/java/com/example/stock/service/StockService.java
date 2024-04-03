package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

  private final StockRepository stockRepository;


  @Transactional
  /**
   *  synchronized로는 해결안됨 -> @Transactional 때문
   *  스프링에서는 Transactional Annotation을 이용하면 우리가 만든 클래스를
   *  래핑한 클래스를 새로 만들어서 실행하게 된다.
   *  스탁 서비스를 필드로 가지는 클래스를 새로 만들어서 실행.
   *
   *  트랜잭션을 시작한 후에 메소드를 호출하고 메소드 실행이 종료가 된다면 트랜잭션을 종료
   *  트랜잭션 종료 시점에 데이터베이스에 업데이트
   *
   *  디크리지 메소드가 완료가 되었고 실제 데이터베이스가 업데이트 되기 전에
   *  다른 Thread가 Decrease Method를 호출할 수 있다.
   *
   *  -> 다른 Thread는 갱신되기 전에 값을 가져가서 이전과 동일한 문제가 발생하는 것이다.
   *  -> @Transaction 없으면 해결됨.
   */
  public synchronized void decrease(Long id, Long quantity) {
    // Stock 조회
    // 재고를 감소
    // 갱신된 값을 저장
    Stock stock = stockRepository.findById(id).orElseThrow();

    stock.decrease(quantity);

    stockRepository.saveAndFlush(stock);
  }
}