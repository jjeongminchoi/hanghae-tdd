package io.hhplus.tdd.point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static io.hhplus.tdd.point.UserPoint.MAX_POINTS;

@Component
public class PointValidator {

    private static final Logger log = LoggerFactory.getLogger(PointValidator.class);

    public void validate(long point, long amount, TransactionType transactionType) {
        if (transactionType == TransactionType.CHARGE) {
            if (amount <= 0L) {
                throw new BizException("충전할 금액이 없습니다.");
            }
            if (point + amount > MAX_POINTS) {
                throw new BizException("최대 10,000 포인트를 초과할 수 없습니다.");
            }
        } else if (transactionType == TransactionType.USE) {
            if (point <= 0L) {
                throw new BizException("사용 가능한 포인트가 없습니다.");
            }
            if (point - amount < 0L) {
                throw new BizException("포인트 잔고가 부족합니다.");
            }
            if (amount <= 0L) {
                throw new BizException("사용할 포인트가 없습니다.");
            }
        }
    }
}
