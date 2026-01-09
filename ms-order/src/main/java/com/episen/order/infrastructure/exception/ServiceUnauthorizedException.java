package com.episen.order.infrastructure.exception;

public class ServiceUnauthorizedException extends RuntimeException {
    public ServiceUnauthorizedException(String serviceName) {
        super(serviceName);
    }
}