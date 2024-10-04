package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PointServiceImpl implements PointService {

    private final PointValidator pointValidator;
    private final PointRepository pointRepository;

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
        UserPoint userPoint = pointRepository.selectById(id);

        long point = userPoint.point();
        long editPoint = userPoint.addPoint(point, amount);

        pointValidator.validate(point, amount, transactionType);

        pointRepository.insert(id, amount, transactionType, System.currentTimeMillis());
        return pointRepository.insertOrUpdate(id, editPoint);
    }

    @Override
    public UserPoint use(long id, long amount, TransactionType transactionType) {
        UserPoint userPoint = pointRepository.selectById(id);

        long point = userPoint.point();
        long editPoint = userPoint.deductPoints(point, amount);

        pointValidator.validate(point, amount, transactionType);

        pointRepository.insert(id, amount, transactionType, System.currentTimeMillis());
        return pointRepository.insertOrUpdate(id, editPoint);
    }
}
