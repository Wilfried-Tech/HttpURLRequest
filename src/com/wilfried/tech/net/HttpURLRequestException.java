package com.wilfried.tech.net;

public class HttpURLRequestException extends RuntimeException {
    static final long serialVersionUID = -1234567890L;

    public HttpURLRequestException() {
    }

    public HttpURLRequestException(String s) {
        super(s);
    }

    public HttpURLRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpURLRequestException(Throwable cause) {
        super(cause);
    }
}
