/*
 * Copyright (c) 2023 Wilfried-Tech.  All rights reserved.
 */

package com.wilfried.tech.net;

/**
 * {@code HttpURLRequestException} is the class who describes an error that occurs when perform {@link HttpURLRequest}
 *
 * @author Wilfried-Tech
 * @since 1.0
 */
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
