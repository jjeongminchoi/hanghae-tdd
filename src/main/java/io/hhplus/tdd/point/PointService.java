package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {

    UserPoint get(long id);

    List<PointHistory> getHistory(long id);

    UserPoint charge(long id, long amount, TransactionType transactionType);

    UserPoint use(long id, long amount, TransactionType transactionType);
}
