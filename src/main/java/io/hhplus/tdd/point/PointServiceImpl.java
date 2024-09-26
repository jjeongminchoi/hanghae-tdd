package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
@Service
public class PointServiceImpl implements PointService {

    private final PointValidator pointValidator;
    private final PointRepository pointRepository;

    private final Map<Long, Lock> userLocks = new ConcurrentHashMap<>();

    // 유저별 Lock을 가져오는 메서드
    private Lock getUserLock(long userId) {
        //유저별로 Lock을 관리하고, 없으면 새로운 Lock을 생성
        return userLocks.computeIfAbsent(userId, id -> new ReentrantLock(true));
    }

    @Override
    public UserPoint get(long id) {
        return pointRepository.selectById(id);
    }

    @Override
    public List<PointHistory> getHistory(long id) {
        return pointRepository.selectAllByUserId(id);
    }

    @Override
    public UserPoint charge(long id, long amount, TransactionType transactionType) {
        Lock lock = getUserLock(id);
        lock.lock(); // 유저 ID에 해당하는 Lock을 가져온다.
        try {
            UserPoint userPoint = pointRepository.selectById(id);

            long point = userPoint.point();
            long editPoint = userPoint.addPoint(amount);

            pointValidator.validate(point, amount, transactionType);

            pointRepository.insert(id, amount, transactionType, System.currentTimeMillis());
            return pointRepository.insertOrUpdate(id, editPoint);
        } finally {
            lock.unlock(); // 충전이 끝나면 Lock 해제
        }
    }

    @Override
    public UserPoint use(long id, long amount, TransactionType transactionType) {
        Lock lock = getUserLock(id);
        lock.lock(); // 충전 처리 전 Lock 획득
        try {
            UserPoint userPoint = pointRepository.selectById(id);

            long point = userPoint.point();
            long editPoint = userPoint.deductPoints(amount);

            pointValidator.validate(point, amount, transactionType);

            pointRepository.insert(id, amount, transactionType, System.currentTimeMillis());
            return pointRepository.insertOrUpdate(id, editPoint);
        } finally {
            lock.unlock(); // 충전이 끝나면 Lock 해제
        }
    }
}
