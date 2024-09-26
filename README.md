# 동시성 제어 방식 분석 보고서
동시성 제어는 여러 스레드가 동시에 공유 자원에 접근할 때 발생할 수 있는 문제를 해결하기 위한 기법이다.

## 동시성 제어 방식

### 1. synchronized
- **처리 방식**: 메서드 또는 블록에 `synchronized` 키워드를 사용하여 동기화한다. 특정 스레드가 접근할 때 다른 스레드는 대기.
- **장점**: 구현이 간단하며, 자바에서 제공하는 기본적인 동기화 방법이다.
- **단점**: 모든 스레드가 대기하게 되어 성능 저하가 발생할 수 있다. 또한, 교착 상태(Deadlock)가 발생할 수 있다.

### 2. Atomic
- **처리 방식**: `java.util.concurrent.atomic` 패키지에 있는 클래스들을 사용하여 원자적인 작업을 수행한다. 예를 들어, `AtomicInteger`를 사용하면 인크리먼트와 같은 작업을 안전하게 수행할 수 있다.
- **장점**: 락을 사용하지 않기 때문에 성능이 우수하고, 높은 동시성을 지원한다.
- **단점**: 복잡한 상태를 관리할 수 없으며, 단순한 경우에만 사용할 수 있다.

### 3. CompletableFuture
- **처리 방식**: 비동기 작업을 처리하기 위한 기능을 제공하며, 여러 비동기 작업을 조합하여 복잡한 작업을 처리할 수 있다.
- **장점**: 코드가 간결해지고, 비동기 프로그래밍을 보다 쉽게 구현할 수 있다.
- **단점**: 비동기 처리의 복잡성으로 인해 디버깅이 어려울 수 있지만 모든 경우에 적합하지는 않다.

### 4. ConcurrentHashMap
- **처리 방식**: 해시맵에 대한 동시 접근을 지원하는 자료구조이다. 내부적으로 세그먼트로 나누어 작업을 처리하여 성능을 극대화한다.
- **장점**: 높은 동시성을 제공하며, 읽기 작업이 많은 경우 매우 효율적이고, thread-safe하다.
- **단점**: 쓰기 작업이 많은 경우 성능 저하가 발생할 수 있다.

### 5. ReentrantLock
- **처리 방식**: `java.util.concurrent.locks.ReentrantLock` 클래스를 사용하여 스레드가 특정 자원에 대해 동기화된 접근을 허용한다. 
- **장점**: 공정성을 설정할 수 있으며, 스레드가 필요할 때 잠금을 해제할 수 있어 유연한 제어가 가능하다.
- **단점**: 적절한 잠금 해제를 하지 않으면 교착 상태가 발생할 수 있다.

## 선택한 동시성 제어 방식
저는 **`ReentrantLock`**과 **`ConcurrentHashMap`**을 선택하였습니다. 

- **이유**:
  - **ReentrantLock**: synchronized 보다 더 유연한 제어가 가능하고, 경합 상황에서 공정성을 보장(가장 오래된 스레드에 액세스 부여)할 수 있기 때문에 스레드 간의 경합 문제를 효과적으로 해결할 수 있습니다.
  - **ConcurrentHashMap**: 여러 스레드가 동시에 접근할 수 있고 사용자별로 Lock을 관리할 수 있어 사용자별 포인트를 관리하는 데 적합합니다.

## 구현 예시

### ReentrantLock &  ConcurrentHashMap 예시
```java
import java.util.concurrent.locks.ReentrantLock;

public class PointServiceImpl {
    private final Map<Long, Lock> userLocks = new ConcurrentHashMap<>();

    private Lock getUserLock(long userId) {
        return userLocks.computeIfAbsent(userId, id -> new ReentrantLock(true));
    }

    private final ReentrantLock lock = new ReentrantLock();
    private final ConcurrentHashMap<Long, Long> userPoints = new ConcurrentHashMap<>();

    public UserPoint charge(long id, long amount, TransactionType transactionType) {
        Lock lock = getUserLock(id);
        lock.lock();
        try {
            충전 로직
        } finally {
            lock.unlock();
        }
    }
}
```

## 간단한 테스트 예시
```java
/**
 * 같은 유저에 대한 동시 요청 테스트
 * 여러 스레드가 동시에 같은 유저의 포인트를 충전할 때, 동시성 문제가 발생하지 않는지 확인한다.
 */
@Test
void 같은_유저에_대한_동시_요청_테스트() throws InterruptedException {
    long userId = 1L;
    long amount = 1000L;

    // 유저 초기 포인트 설정
    pointService.charge(userId, amount, TransactionType.CHARGE);

    // ExecutorService와 CountDownLatch 설정
    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

    // 동시에 여러 스레드가 같은 유저의 포인트를 충전하고 사용하는 작업 수행
    for (int i = 0; i < THREAD_COUNT; i++) {
        executorService.submit(() -> {
            try {
                pointService.charge(userId, 100L, TransactionType.CHARGE);
            } finally {
                latch.countDown(); // 스레드 작업이 끝나면 countDown한다.
            }
        });
    }

    // 모든 스레드가 작업을 완료할 때까지 대기
    latch.await();

    // 최종 포인트 검증: 최초 1000 + 충전 10번(1000) = 2000
    UserPoint userPoint = pointService.get(userId);
    assertThat(userPoint.point()).isEqualTo(2000L);
}
```

## 결론
동시성 제어 방식은 멀티스레드 환경에서 데이터 일관성을 유지하기 위해 필수적이다.
다양한 동시성 제어 방식 중 `ReentrantLock`과 `ConcurrentHashMap`의 조합은 유연성과 성능을 동시에 만족시킬 수 있는 좋은 조합이라고 생각한다.

이 두 가지 방식은 서로 보완적인 특성을 가지고 있어, 포인트 충전과 사용 작업에서 발생할 수 있는 경합 문제를 효과적으로 해결할 수 있었다.

향후에는 성능 최적화를 위해 이러한 방식을 적절히 조합하여 더욱 효율적인 동시성 제어를 구현할 계획입니다.
