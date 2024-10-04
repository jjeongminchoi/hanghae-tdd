package io.hhplus.tdd;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    private PointServiceImpl pointService;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointValidator pointValidator;

    @Mock
    private UserPoint userPoint;

    @Mock
    private PointHistory pointHistory;

    /**
     * 포인트가 정상적으로 조회되는지 테스트합니다.
     */
    @Test
    void get() {
        //given
        UserPoint mockUserPoint = new UserPoint(1L, 100L, System.currentTimeMillis());

        //stub
        when(pointRepository.selectById(1L)).thenReturn(mockUserPoint);

        //when
        UserPoint userPoint = pointService.get(1L);

        //then
        assertThat(userPoint.point()).isEqualTo(100L);
    }

    /**
     * 포인트 히스토리가 정상적으로 조회되는지 테스트합니다.
     */
    @Test
    void getHistory() {
        //given
        List<PointHistory> mockUserPointHistory = List.of(
                new PointHistory(1L, 1L, 100, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, 1L, 200, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(3L, 1L, 50, TransactionType.USE, System.currentTimeMillis())
        );

        //stub
        when(pointRepository.selectAllByUserId(1L)).thenReturn(mockUserPointHistory);

        //when
        List<PointHistory> pointHistories = pointService.getHistory(1L);

        //then
        assertThat(pointHistories.size()).isEqualTo(3);
        assertThat(pointHistories.get(0).amount()).isEqualTo(100L);
    }

    /**
     * 포인트가 정상적으로 충전되는지 테스트합니다.
     */
    @Test
    void chargePoint() {
        //given
        long currentTimeMillis = System.currentTimeMillis();
        UserPoint mockUserPoint = new UserPoint(1L, 100L, currentTimeMillis);
        UserPoint mockUserPoint2 = new UserPoint(1L, 150L, currentTimeMillis);

        //stub
        when(pointRepository.selectById(1L)).thenReturn(mockUserPoint);
        when(pointRepository.insertOrUpdate(1L, 150L)).thenReturn(mockUserPoint2);

        //when
        UserPoint userPoint = pointService.charge(1L, 50L, TransactionType.CHARGE);

        //then
        assertThat(userPoint.point()).isEqualTo(150L);
    }

    /**
     * 포인트가 정상적으로 사용되는지 테스트합니다.
     */
    @Test
    void usePoint() {
        //given
        long currentTimeMillis = System.currentTimeMillis();
        UserPoint mockUserPoint = new UserPoint(1L, 100L, currentTimeMillis);
        UserPoint mockUserPoint2 = new UserPoint(1L, 50L, currentTimeMillis);

        //stub
        when(pointRepository.selectById(1L)).thenReturn(mockUserPoint);
        when(pointRepository.insertOrUpdate(1L, 50L)).thenReturn(mockUserPoint2);

        //when
        UserPoint userPoint = pointService.use(1L, 50L, TransactionType.USE);

        //then
        assertThat(userPoint.point()).isEqualTo(50L);
    }
}
