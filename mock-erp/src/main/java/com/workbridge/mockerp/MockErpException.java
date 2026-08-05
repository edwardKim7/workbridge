package com.workbridge.mockerp;

public class MockErpException extends RuntimeException {
    private final int status;

    public MockErpException(int s, String m) {
        super(m);
        status = s;
    }

    public int status() {
        return status;
    }
}
