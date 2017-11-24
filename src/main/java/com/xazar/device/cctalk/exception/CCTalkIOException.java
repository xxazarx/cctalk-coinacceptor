package com.xazar.device.cctalk.exception;

import java.io.IOException;

public class CCTalkIOException extends IOException {
    public CCTalkIOException() {
        super();
    }

    public CCTalkIOException(String message) {
        super(message);
    }

    public CCTalkIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public CCTalkIOException(Throwable cause) {
        super(cause);
    }
}
