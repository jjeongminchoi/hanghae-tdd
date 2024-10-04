package io.hhplus.tdd.domain.point;

import io.hhplus.tdd.interfaces.api.point.TransactionType;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
}
