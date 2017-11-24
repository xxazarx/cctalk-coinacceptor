package com.xazar.device.cctalk.exception;

public class InvalidMessageException extends Exception {
	
    private static final long serialVersionUID = -3258256268402518828L;

    public InvalidMessageException(String message) {
	    super(message);
    }

	@Override
    public String toString() {
	    return "InvalidMessageException [message=" + getMessage() + "]";
    }
}
