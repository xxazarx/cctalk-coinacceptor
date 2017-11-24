package com.xazar.device.cctalk.exception;

public class CCTalkDeviceException extends Exception {
    public CCTalkDeviceException() {
        super();
    }

    public CCTalkDeviceException(String message) {
        super(message);
    }

    public CCTalkDeviceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CCTalkDeviceException(Throwable cause) {
        super(cause);
    }
}
