package com.episen.order.infrastructure.exception;

public class ServiceForbiddenException extends RuntimeException {
    public ServiceForbiddenException(String service) {
        super(service);
    }
}
