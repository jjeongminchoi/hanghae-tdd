package io.hhplus.tdd;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @Test
    public void 유저_포인트_조회() {
        //given
        pointRepository.insertOrUpdate(1L, 100L);

        //when
        UserPoint result = pointService.get(1L);

        //then
        assertThat(result.point()).isEqualTo(100L);
    }

    @Test
    public void 유저_포인트_충전내역_조회() {
        //given
        pointRepository.insert(1L, 50L, TransactionType.CHARGE, System.currentTimeMillis());
        pointRepository.insert(1L, 100L, TransactionType.CHARGE, System.currentTimeMillis());
        pointRepository.insert(1L, 50L, TransactionType.USE, System.currentTimeMillis());

        //when
        List<PointHistory> result = pointService.getHistory(1L);

        //then
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    void 포인트_충전() {
        //given
        pointRepository.insertOrUpdate(1L, 100L);

        //when
        UserPoint result = pointService.charge(1L, 100L, TransactionType.CHARGE);

        //then
        assertThat(result.point()).isEqualTo(200L);
    }

    @Test
    void 포인트_사용() {
        //given
        pointRepository.insertOrUpdate(1L, 100L);

        //when
        UserPoint result = pointService.use(1L, 50L, TransactionType.USE);

        //then
        assertThat(result.point()).isEqualTo(50L);
    }

    @Test
    void 포인트_충전시_충전할_금액이_0_보다_작거나_같으면_예외를_발생시킨다() {
        //throw
        assertThatThrownBy(() -> pointService.charge(1L, 0L, TransactionType.CHARGE))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("충전할 금액이 없습니다.");
    }

    @Test
    void 포인트_충전시_최대_10000_포인트를_초과할_수_없습니다() {
        //given
        pointRepository.insertOrUpdate(1L, 5000L);

        //throw
        assertThatThrownBy(() -> pointService.charge(1L, 5001L, TransactionType.CHARGE))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("최대 10,000 포인트를 초과할 수 없습니다.");
    }

    @Test
    void 포인트_사용시_사용_가능한_포인트가_없을_때() {
        //given
        pointRepository.insertOrUpdate(1L, 0L);

        //throw
        assertThatThrownBy(() -> pointService.use(1L, 1L, TransactionType.USE))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("사용 가능한 포인트가 없습니다.");
    }

    @Test
    void 포인트_사용시_포인트_잔고가_부족할때() {
        //given
        pointRepository.insertOrUpdate(1L, 1000L);

        //throw
        assertThatThrownBy(() -> pointService.use(1L, 1001L, TransactionType.USE))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("포인트 잔고가 부족합니다.");
    }

    /**
     * 포인트 사용 시, 사용할 포인트가 없을 때
     */
    @Test
    void 포인트_사용시_사용할_포인트_없을때() {
        //given
        pointRepository.insertOrUpdate(1L, 1000L);

        //throw
        assertThatThrownBy(() -> pointService.use(1L, 0L, TransactionType.USE))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("사용할 포인트가 없습니다.");
    }
}

