package io.hhplus.tdd.point;

public class BizException extends RuntimeException {
    private String msg;

    public BizException(String msg) {
        this.msg = msg;
    }
}
