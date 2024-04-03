package com.zb.tablereservation.stock.repository;

import com.zb.tablereservation.stock.domain.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface StockRepository extends JpaRepository<Stock, Long> {

  @Lock(value = LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Stock s where s.id=:id")
  Stock findByIdWithPessimisticLock(Long id);

  @Lock(value = LockModeType.OPTIMISTIC)
  @Query("select s from Stock s where s.id = :id")
  Stock findByIdWithOptimisticLock(Long id);
}