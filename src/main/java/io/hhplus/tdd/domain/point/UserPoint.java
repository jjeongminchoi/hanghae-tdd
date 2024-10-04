package io.hhplus.tdd.domain.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {
    public static final long MAX_POINTS = 10000;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public long addPoint(long amount) {
        return this.point + amount;
    }

    public long deductPoints(long amount) {
        return this.point - amount;
    }
}
