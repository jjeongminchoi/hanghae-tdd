package io.hhplus.tdd.domain.point;

import io.hhplus.tdd.interfaces.api.point.TransactionType;

import java.util.List;

public interface PointService {

    UserPoint get(long id);

    List<PointHistory> getHistory(long id);

    UserPoint charge(long id, long amount, TransactionType transactionType);

    UserPoint use(long id, long amount, TransactionType transactionType);
}
