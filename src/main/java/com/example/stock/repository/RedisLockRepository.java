package com.example.stock.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis의 명령어를 이용하기 위한 RedisLockRepository
 */
@Component
@RequiredArgsConstructor
public class RedisLockRepository {
  // 레디스의 명령어를 실행을 위한 레디스 템플릿을 변수
  private final RedisTemplate<String, String> redisTemplate;

  /**
   * 로직 실행 전 key와 setnx 명령어를 활용해서 락
   * 키에는 stockID를 넣어줄 것이고 value는 lock이라는 문자를 넣어줄 것이다.
   * @param key 키에 사용할 변수
   * @return 성공,실패
   */
  public Boolean lock(Long key) {
    return redisTemplate
        .opsForValue()
        .setIfAbsent(generateKey(key), "lock", Duration.ofMillis(3_000));
  }

  /**
   * 로직이 끝나면 unlock
   * @param key
   * @return
   */
  public Boolean unlock(Long key) {
    return redisTemplate.delete(generateKey(key));
  }

  private String generateKey(Long key) {
    return key.toString();
  }
}
