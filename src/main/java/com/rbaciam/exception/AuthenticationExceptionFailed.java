package com.rbaciam.exception;

public class AuthenticationExceptionFailed extends RuntimeException {
    public AuthenticationExceptionFailed(String message) {
        super(message);
    }

    public AuthenticationExceptionFailed(String message, Throwable cause) {
        super(message, cause);
    }
}