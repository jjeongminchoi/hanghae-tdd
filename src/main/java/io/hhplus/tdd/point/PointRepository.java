package io.hhplus.tdd.point;

import java.util.List;

public interface PointRepository {

    UserPoint selectById(long id);

    List<PointHistory> selectAllByUserId(long id);

    UserPoint insertOrUpdate(long id, long amount);

    void insert(long userId, long amount, TransactionType type, long updateMillis);
}
