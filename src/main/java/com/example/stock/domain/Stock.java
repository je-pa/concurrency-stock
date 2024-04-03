package com.example.stock.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Getter;

@Entity
public class Stock {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long productId;

  @Getter
  private Long quantity;

  // optimistic lock을 위한 version 추가
  @Version
  private Long version;

  public Stock() {

  }

  public void decrease(Long quantity) {
    if (this.quantity - quantity < 0) {
      throw new RuntimeException("foo");
    }

    this.quantity -= quantity;
  }

  public Stock(Long productId, Long quantity) {
    this.productId = productId;
    this.quantity = quantity;
  }
}