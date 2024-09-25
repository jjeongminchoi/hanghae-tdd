package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public long charge(long point, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전할 금액이 없습니다.");
        }
        return point + amount;
    }

    public long use(long point, long amount) {
        if (point <= 0) {
            throw new IllegalArgumentException("사용할 금액이 없습니다.");
        }
        return point - amount;
    }
}
