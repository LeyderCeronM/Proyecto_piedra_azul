package com.medical.client.service;

public class ServiceException extends Exception {
    private int statusCode = -1;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(int statusCode, String body) {
        super("HTTP " + statusCode + ": " + body);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
