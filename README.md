# 동시성 이슈 해결 알아보기

데이터 정합성을 맞추는 방법에 대해 알아보자.

## 1. synchronized
- 관련 코드: [StockService](src/main/java/com/example/stock/service/StockService.java)
- synchronized를 사용하면 하나의 프로세스 안에서 스레드 동시 접근을 막는다.

### synchronized를 사용할 때 주의점이 있다.
1. @Transactional를 사용할 경우
   - 스프링에서 Transactional Annotation을 이용하면 우리가 만든 클래스를
     래핑한 클래스를 새로 만들어서 실행하게 된다.
  
   - 코드상에서는 stockservice를 필드로 가지는 클래스를 새로 만들어서 실행한다.
  
   - 트랜잭션을 시작한 후에 메소드를 호출하고 메소드 실행이 종료가 된다면 트랜잭션을 종료하는데,
    
     트랜잭션 종료 시점에 데이터베이스에 업데이트업데이트 한다.

   - 메소드가 완료가 되었고 실제 데이터베이스가 업데이트 되기 전에 다른 Thread가 Decrease Method를 호출할 수 있다.
     - 다른 Thread는 갱신되기 전에 값을 가져가서 이전과 동일한 문제가 발생하는 것이다.
   - 이는 @Transaction 없으면 해결되긴 한다.
2. Java의 Synchronized는 하나의 프로세스 안에서만 보장한다.
    - 서버가 1대일 때는 데이터의 접근을 서버가 1대만 해서 괜찮겠지만,
      
      서버가 2대 혹은 그 이상일 경우는 데이터의 접근을 여러 대에서 할 수 있게 된다.
   
      싱크로나이즈드는 각 프로세스 안에서만 보장이 되기 때문에 
    
      결국 여러 스레드에서 동시에 데이터에 접근을 할 수 있게 되면서 레이스 컨디션이 발생하게 된다.

> 실제 운영 중인 서비스는 대부분 2대 이상의 서버를 사용하기 때문에
> Synchronized는 거의 사용하지 않는다.

## 2. Mysql 활용한 방법

1. Pessimistic Lock
    - 관련 코드: [PessimisticLockStockService](src/main/java/com/example/stock/service/PessimisticLockStockService.java)
    - 데이터에 Lock을 걸어 정합성을 맞추는 방법
      - `exclusive lock`을 걸게되면 다른 트랜잭션에서는 락이 해제되기 전에 데이터를 가져갈 수 없다.
        > 주의: 데드락
        > 
        > 서버가 여러 대가 있을 때 서버 1이 락을 걸고 데이터를 가져가게 되면 
        > 나머지 서버는 서버 1이 락을 해제하기 전까지 데이터를 가져갈 수 없다.
    - 장점: 충돌이 빈번하게 일어난다면 OptimisticLock보다 성능이 좋을 수 있다.
    - 단점: 별도의 락을 잡기 때문에 성능 감소가 있을 수 있다.
2. OptimisticLock
    - 관련코드: [OptimisticLockStockFacade](src/main/java/com/example/stock/facade/service/OptimisticLockStockFacade.java)
    - 실제로 락을 이용하지 않고 버전을 이용함으로써 정합성을 맞추는 방법
      - 먼저 데이터를 읽은 후 업데이트를 수행할 때 현재 내가 읽은 버전이 맞는지 확인하며 업데이트하는데, 
      
        만약 읽은 버전에서 수정사항이 생겼을 경우 어플리케이션에서 다시 읽은 후 작업 수행한다.
      > 1. 서버1이 데이터베이스에서 버전 1인 데이터를 읽어온다.
      > 2. 읽고 난 후 서버1이 업데이트 쿼리를 날린다.
      >   - 이때 update에 version +1 을 해주고 where에는 조건에 버전1 을 명시해주면서 업데이트 한다. 
      >     ```mysql
      >         update set version = version+1, quantity = 2
      >         from table 
      >         where version =1
      >     ```
      > 3. 서버2가 동일한 업데이트를 한다. 이때 version이 1이 아니기 때문에 수행이 되지 않는다.
      > 4. 업데이트가 실패하게 되면서 실제 어플리케이션에서 다시 읽은 후에 작업을 수행해야 하는 로직을 한다.
      >   - 내가 읽은 버전에서 수정사항이 생겼을 경우에는 애플리케이션에서 다시 읽은 후에 작업 수행한다는 뜻
    - 장점: 별도의 락을 잡지 않으므로 페시미스팅 락보다 성능상 이점
    - 단점: 업데이트가 실패했을 때 재시도 로직을 개발자가 직접 작성해 주어야 하는 번거로움
> 충돌이 빈번하게 일어난다면 혹은 충돌이 빈번하게 일어날 것이라고 예상된다면 Pessimistic Lock
> 
> 빈번하게 일어나지 않을 것이라고 예상된다면 OptimisticLock
3. Named Lock
    - 관련 코드: [NamedLockStockFacade](src/main/java/com/example/stock/facade/service/NamedLockStockFacade.java)
        > 실제로 사용하실 때는 데이터 소스를 분리해서 사용
        >
        > 같은 데이터 소스를 사용하면 커넥션 풀이 부족해지는 현상으로 인해서 다른 서비스에도 영향을 끼친다.
      - 이름을 가진 metadata locking
      - 이름을 가진 락을 획득한 후 해제할 때까지 다른 `session`은 이 락을 획득할 수 없도록 한다.
          - 별도의 공간에 락을 건다.
      - Pessimistic Lock은 row나 table단위로 lock을 걸지만, named lock은 메타데이터에 락킹을 하는 방법이다.
      - MySQL에서는 get-lock 명령어를 통해 named-lock을 획득할 수 있고 release-lock 명령어를 통해 lock을 해제할 수 있다.
        
    - Named Lock은 주로 분산락을 구현할 때 사용
    - 장점: Pessimistic Lock은 타임아웃을 구현하기 힘들지만 Named Lock은 타임아웃을 손쉽게 구현
    - 단점: 트랜잭션이 종료될 때 락이 자동으로 해제되지 않기 때문에 별도의 명령으로 해제를 수행해주거나 선점 시간이 끝나야 해제가 된다.
    > 트랜직션 종료 시에 락 해제, 세션 관리를 잘 해줘야 되기 때문에 주의해서 사용해야 하고 실제 구현 방법이 복잡할 수 있다.