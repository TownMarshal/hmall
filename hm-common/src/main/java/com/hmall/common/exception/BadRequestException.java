package com.hmall.common.exception;

/**
 * @author Administrator
 */
public class BadRequestException extends CommonException{

    public BadRequestException(String message) {
        super(message, 400);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause, 400);
    }

    public BadRequestException(Throwable cause) {
        super(cause, 400);
    }
}
