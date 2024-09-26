package io.hhplus.tdd;

import io.hhplus.tdd.point.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class PointServiceConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(PointServiceConcurrencyTest.class);

    @Autowired
    private PointService pointService;

    private final int THREAD_COUNT = 10; // 동시에 실행할 스레드 개수

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

    /**
     * 같은 유저에 대한 동시 요청 테스트 2
     * 여러 스레드가 동시에 같은 유저의 포인트를 충전하거나 사용할 때, 최대 충전(10000)을 넘어서면 예외를 발생한다.
     */
    @Test
    void 같은_유저에_대한_동시_요청_테스트_2() throws InterruptedException {
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
                    pointService.charge(userId, 1000L, TransactionType.CHARGE);
                } catch (BizException e) {
                    log.error("Exception occurred when charging point", e.getMessage());
                } finally {
                    latch.countDown(); // 스레드 작업이 끝나면 countDown한다.
                }
            });
        }

        // 모든 스레드가 작업을 완료할 때까지 대기
        latch.await();

        // 마지막 스레드에서 충전시 11000원이 되므로 "최대 10,000 포인트를 초과할 수 없습니다." 예외가 발생한다.
        assertThatThrownBy(() -> pointService.charge(userId, 1000L, TransactionType.CHARGE))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("최대 10,000 포인트를 초과할 수 없습니다.");
    }

    /**
     * 다른 유저에 대한 동시 요청 테스트
     * 여러 스레드가 서로 다른 유저에 대해 포인트를 충전하거나 사용할 때, 병렬로 처리되는지 확인한다.
     */
    @Test
    void 다른_유저에_대한_동시_요청_테스트() throws InterruptedException {
        long amount = 1000L;

        // 여러 유저의 ID 리스트 생성
        ArrayList<Long> userIds = new ArrayList<>();
        for (long i = 1L; i <= THREAD_COUNT; i++) {
            userIds.add(i);
            pointService.charge(i, amount, TransactionType.CHARGE); // 각 유저마다 초기 포인트 충전
        }

        // ExecutorService와 CountDownLatch 설정
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        // 여러 스레드가 각각 다른 유저의 포인트를 충전하고 사용하는 작업 수행
        for (long userId : userIds) {
            executorService.submit(() -> {
                try {
                    pointService.charge(userId, 100L, TransactionType.CHARGE);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 작업을 완료할 때까지 대기
        latch.await(5, TimeUnit.SECONDS); // 타임아웃 설정

        // 각 유저의 최종 포인트 검증: 최초 1000 + 충전(100) = 1100
        for (long userId : userIds) {
            UserPoint userPoint = pointService.get(userId);
            assertThat(userPoint.point()).isEqualTo(1100L);
        }
    }
}
